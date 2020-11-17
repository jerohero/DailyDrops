CREATE TABLE user_likes (
    id TEXT PRIMARY KEY
);

CREATE TABLE user_drops (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    note TEXT,
    date INTEGER NOT NULL,
    has_image INT NOT NULL
);