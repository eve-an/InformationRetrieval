CREATE TABLE IF NOT EXISTS premise (
  pID SERIAL PRIMARY KEY,
  dID INT REFERENCES discussion NOT NULL,
  crawlID TEXT NOT NULL UNIQUE,
  title TEXT
);