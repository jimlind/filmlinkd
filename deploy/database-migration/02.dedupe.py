#!/usr/bin/env python3
import argparse
import csv
from collections import OrderedDict
from pathlib import Path

USER_PK = "letterboxd_id"
CHANNEL_PK = ("letterboxd_id", "channel_id")
ENTRY_PK = ("letterboxd_id", "entry_lid")
VIP_PK = "channel_id"

def pick_user(left: dict, right: dict) -> dict:
    for field in ("created", "updated", "checked"):
        left_value = int(left.get(field) or 0)
        right_value = int(right.get(field) or 0)
        if left_value != right_value:
            return left if left_value > right_value else right
    return left

def dedupe(path: Path, pk, user_rule: bool) -> int:
    seen: OrderedDict = OrderedDict()
    with path.open(encoding="utf-8", newline="") as fh:
        reader = csv.DictReader(fh)
        headers = reader.fieldnames
        for row in reader:
            key = tuple(row[f] for f in pk) if isinstance(pk, tuple) else row[pk]
            if key not in seen:
                seen[key] = row
            elif user_rule:
                seen[key] = pick_user(seen[key], row)

    with path.open("w", encoding="utf-8", newline="") as fh:
        writer = csv.DictWriter(fh, fieldnames=headers)
        writer.writeheader()
        writer.writerows(seen.values())
    return len(seen)

def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--csv-dir", required=True, help="Directory of migration CSV files")
    args = parser.parse_args()

    csv_dir = Path(args.csv_dir)
    dedupe(csv_dir / "users.csv", USER_PK, user_rule=True)
    dedupe(csv_dir / "user_channels.csv", CHANNEL_PK, user_rule=False)
    dedupe(csv_dir / "user_previous_entries.csv", ENTRY_PK, user_rule=False)
    dedupe(csv_dir / "vip_channels.csv", VIP_PK, user_rule=False)

if __name__ == "__main__":
    main()
