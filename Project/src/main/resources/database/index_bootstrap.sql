
-- normal argument search
CREATE TABLE IF NOT EXISTS argument_index (
  tID INT REFERENCES token NOT NULL,
  argID INT REFERENCES argument NOT NULL,
  weight SMALLINT DEFAULT 0,
  occurrences INT NOT NULL,
  offsets SMALLINT ARRAY NOT NULL,
  PRIMARY KEY(tID, argID)
);

CREATE INDEX IF NOT EXISTS arg_index_idx ON argument_index (tID) INCLUDE (argID);

-- titles have a higher priority than arguments so when 
-- a match occurrences in the premise or discussion we have to
-- query all arguments to this premise/discussion. Or arguments
-- might have a perfect match but the premise and discussion title
-- are different this could mean that the search result is false positive

CREATE TABLE IF NOT EXISTS premise_index (
  tID INT REFERENCES token NOT NULL,
  pID INT REFERENCES premise NOT NULL,
  weight SMALLINT DEFAULT 0,
  occurrences INT NOT NULL,
  offsets SMALLINT ARRAY NOT NULL,
  PRIMARY KEY(tID, pID)
);
CREATE INDEX IF NOT EXISTS premise_index_idx ON premise_index (tID) INCLUDE (pID);

CREATE TABLE IF NOT EXISTS discussion_index (
  tID INT REFERENCES token NOT NULL,
  dID INT REFERENCES discussion NOT NULL,
  weight SMALLINT DEFAULT 0,
  occurrences INT NOT NULL,
  offsets SMALLINT ARRAY NOT NULL,
  PRIMARY KEY(tID, dID)
);
CREATE INDEX IF NOT EXISTS discussion_index_idx ON discussion_index (tID) INCLUDE (dID);