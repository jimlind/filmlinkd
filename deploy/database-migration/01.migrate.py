#!/usr/bin/env python3
import argparse
import csv
import json
from collections import defaultdict
from pathlib import Path
from typing import Any

def load_export(json_dir: str):
    files = sorted(Path(json_dir).glob("output-*.json"))
    merged = []
    if not files:
        raise SystemExit(f"No output-*.json files found in {json_dir}")
    if len(files) > 1:
        raise SystemExit(f"More than one output-*.json files found in {json_dir}")

    with files[0].open(encoding="utf-8") as fh:
        tree = json.load(fh)

    merged = defaultdict(dict)
    for collection, docs in tree.items():
        for doc_id, fields in docs.items():
            merged[collection][doc_id] = fields

    return dict(merged)

def _clean_str(value):
    if value is None:
        return None
    value = str(value)
    return value if value else None

def _clean_int(value):
    if value is None:
        return 0
    try:
        return int(value)
    except:
        return 0

def map_users(tree: dict) -> tuple[list[dict], list[dict], list[dict]]:
    users: list[dict] = []
    channels: list[dict] = []
    entries: list[dict] = []

    for doc_id, fields in tree.get("users", {}).items():
        letterboxd_id = _clean_str(fields.get("letterboxdId"))
        previous = fields.get("previous") or {}
        previous_list = previous.get("list") or []
        footer = fields.get("footer") or {}
        users.append(
            {
                "letterboxd_id": letterboxd_id,
                "user_name": _clean_str(fields.get("userName")) or "",
                "display_name": _clean_str(fields.get("displayName")) or "",
                "image": _clean_str(fields.get("image")),
                "created": _clean_int(fields.get("created")),
                "updated": _clean_int(fields.get("updated")),
                "checked": _clean_int(fields.get("checked")),
                "footer_icon": _clean_str(footer.get("icon")),
                "footer_text": _clean_str(footer.get("text")),
            }
        )

        for channel in fields.get("channelList") or []:
            channel_id = _clean_str(channel.get("channelId"))
            if channel_id:
                channels.append({"letterboxd_id": letterboxd_id, "channel_id": channel_id, "archived_at": None})

        for channel in fields.get("archivedChannelList") or []:
            channel_id = _clean_str(channel.get("channelId"))
            if channel_id:
                channels.append({"letterboxd_id": letterboxd_id, "channel_id": channel_id, "archived_at": 1})

        if previous_list:
            for entry_lid in previous_list:
                if entry_lid:
                    entries.append({"letterboxd_id": letterboxd_id, "entry_lid": str(entry_lid)})
        else:
            fallback = _clean_str(previous.get("lid"))
            if fallback and fallback != "0":
                entries.append({"letterboxd_id": letterboxd_id, "entry_lid": fallback})

    return users, channels, entries

USER_COLUMNS = ["letterboxd_id", "user_name", "display_name", "image", "created","updated", "checked", "footer_icon", "footer_text"]
CHANNEL_COLUMNS = ["letterboxd_id", "channel_id", "archived_at"]
ENTRY_COLUMNS = ["letterboxd_id", "entry_lid"]

def write_csv(rows: list[dict], columns: list[str], path: Path) -> None:
    with path.open("w", encoding="utf-8", newline="") as fh:
        writer = csv.writer(fh)
        writer.writerow(columns)
        for row in rows:
            writer.writerow(row.get(col) for col in columns)
    print(f"Wrote {path}")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--json-dir", required=True, help="Directory of output-*.json files from fs_to_json.py")
    parser.add_argument("--csv-dir", required=True, help="Write COPY-ready CSV files to this directory")
    args = parser.parse_args()

    tree = load_export(args.json_dir)
    users, channels, entries = map_users(tree)

    out_dir = Path(args.csv_dir)
    out_dir.mkdir(parents=True, exist_ok=True)
    write_csv(users, USER_COLUMNS, out_dir / "users.csv")
    write_csv(channels, CHANNEL_COLUMNS, out_dir / "user_channels.csv")
    write_csv(entries, ENTRY_COLUMNS, out_dir / "user_previous_entries.csv")

    print(f"users: {len(users)}  user_channels: {len(channels)}  user_previous_entries: {len(entries)}")

if __name__ == "__main__":
    main()
