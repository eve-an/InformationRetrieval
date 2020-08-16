-- source contains information about the source of discussion
CREATE TABLE IF NOT EXISTS source (
  sourceID SERIAL PRIMARY KEY,
  domain TEXT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS source_idx ON source (domain) INCLUDE (sourceID);