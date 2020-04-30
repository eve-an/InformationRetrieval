-- in java map to those we have in db
-- https://stackoverflow.com/questions/1833252/java-stanford-nlp-part-of-speech-labels
-- https://nlp.stanford.edu/nlp/javadoc/javanlp/

-- VB       - verb in base form
-- NN       - noun
-- RB       - adverb
-- UH       - interjection
-- CC       - conjunection
-- CD       - numeral or cardinal
-- JJ       - adjective
-- PRP      - pronoun
-- IN       - preposition
-- DT       - determiner
-- OTHER    - if nothing fits

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'class') THEN
        CREATE TYPE class AS ENUM ('VB', 'NN', 'RB', 'UH', 'CC', 'CD', 'JJ', 'PRP', 'IN', 'DT', 'OTHER');
    END IF;
END$$;



CREATE TABLE IF NOT EXISTS token (
  tID SERIAL PRIMARY KEY,
  token TEXT UNIQUE NOT NULL,
  totalCounter INT DEFAULT 0,
  argumentCounter INT DEFAULT 0,
  premiseCounter INT DEFAULT 0,
  discussionCounter INT DEFAULT 0,
  languageFrequency DECIMAL(5, 3) DEFAULT NULL, -- max 99.999
  class class DEFAULT 'OTHER'
);

CREATE UNIQUE INDEX IF NOT EXISTS token_id_idx ON token (token) INCLUDE (tID);


-- idee vlt. partition by class with list VALUES IN ('VB') but this requires knowing the class before querying
-- and also a correct classification. (seems bad) 
-- log(1000000)=23 < 11*log(1000)=110