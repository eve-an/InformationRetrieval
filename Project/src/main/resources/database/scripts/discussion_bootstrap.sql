--- a discussion contains multiple premises under one title in a given source
CREATE TABLE IF NOT EXISTS discussion (
  dID SERIAL PRIMARY KEY,
  sourceID INT REFERENCES source NOT NULL,
  crawlID TEXT UNIQUE NOT NULL,
  title TEXT NOT NULL,
  url TEXT,
  length INT
);
