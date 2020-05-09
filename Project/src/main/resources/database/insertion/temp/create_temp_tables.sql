CREATE SCHEMA IF NOT EXISTS temp AUTHORIZATION irargdb;

CREATE TABLE IF NOT EXISTS temp.source
(
    sourceID SERIAL,
    domain   TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS temp.discussion
(
    dID      SERIAL,
    sourceID INT,
    crawlID  TEXT NOT NULL,
    title    TEXT NOT NULL,
    url      TEXT
);

CREATE TABLE IF NOT EXISTS temp.premise
(
    pID     SERIAL,
    dID     INT,
    crawlID TEXT NOT NULL,
    title   TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS temp.argument
(
    argID       SERIAL,
    pID         INT,
    crawlID     TEXT    NOT NULL,
    content     text    NOT NULL,
    totalTokens INT     NOT NULL,
    isPro       BOOLEAN NOT NULL
);

-- Update sequences
SELECT setval('temp.source_sourceid_seq', max(sourceid))
FROM source;

SELECT setval('temp.discussion_did_seq', max(did))
FROM discussion;

SELECT setval('temp.premise_pid_seq', max(pid))
FROM premise;

SELECT setval('temp.argument_argid_seq', max(argid))
FROM argument;