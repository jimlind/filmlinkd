CREATE TABLE users (
  letterboxd_id         TEXT PRIMARY KEY,
  user_name             TEXT NOT NULL,
  display_name          TEXT NOT NULL,
  image                 TEXT,
  created               BIGINT NOT NULL DEFAULT 0,
  updated               BIGINT NOT NULL DEFAULT 0,
  checked               BIGINT NOT NULL DEFAULT 0,
  footer_icon           TEXT,
  footer_text           TEXT,
  letterboxd_id_swapped TEXT GENERATED ALWAYS AS (
                          translate(letterboxd_id,
                            'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ',
                            'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz')
                        ) STORED
);
CREATE INDEX idx_users_lid_order
  ON users (length(letterboxd_id), letterboxd_id_swapped);

CREATE TABLE user_channels (
  letterboxd_id TEXT NOT NULL REFERENCES users(letterboxd_id) ON DELETE CASCADE,
  channel_id    BIGINT NOT NULL,
  archived_at   BIGINT,
  PRIMARY KEY (letterboxd_id, channel_id)
);
CREATE INDEX idx_user_channels_active ON user_channels (channel_id) WHERE archived_at IS NULL;

CREATE TABLE user_previous_entries (
  letterboxd_id     TEXT NOT NULL REFERENCES users(letterboxd_id) ON DELETE CASCADE,
  entry_lid         TEXT NOT NULL,
  entry_lid_swapped TEXT GENERATED ALWAYS AS (
                       translate(entry_lid,
                         'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ',
                         'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz')
                     ) STORED,
  PRIMARY KEY (letterboxd_id, entry_lid)
);

CREATE TABLE vip_channels (
  channel_id BIGINT PRIMARY KEY,
  group_name TEXT
);
