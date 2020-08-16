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

CREATE TABLE IF NOT EXISTS token (
  tID SERIAL PRIMARY KEY,
  token TEXT UNIQUE NOT NULL,
  languageFrequency DECIMAL(5, 3) DEFAULT NULL -- max 99.999
);

CREATE UNIQUE INDEX IF NOT EXISTS token_id_idx ON token (token) INCLUDE (tID);
