-- PREPARE CREATION OF CONSTRAINTS
-- duplicate temp_discussions
DELETE
FROM temp.discussion
WHERE crawlid IN (
    SELECT crawlid
    FROM temp.discussion
    GROUP BY crawlid
    HAVING count(*) > 1);

-- duplicate temp_premises
DELETE
FROM temp.premise
WHERE crawlid IN (
    SELECT crawlid
    FROM temp.premise
    GROUP BY crawlid
    HAVING count(*) > 1);


-- duplicate temp_arguments
DELETE
FROM temp.argument
WHERE crawlid IN (SELECT crawlid
                  FROM temp.argument
                  GROUP BY crawlid
                  HAVING count(*) > 1);

-- Set temp_temp_sourceid in temp_discussion
UPDATE temp.discussion d
SET (sourceid) = (
    SELECT sourceid
    FROM temp.source
    ORDER BY sourceid DESC
    LIMIT 1);


UPDATE temp.premise AS p
SET did = d.did
FROM temp.discussion AS d
WHERE d.crawlid = regexp_replace(p.crawlid, '-.*', '');


-- Set pid in temp_argument
UPDATE temp.argument a
SET pid = p.pid
FROM temp.premise p
WHERE a.crawlid = p.crawlid;



-- CREATE CONSTRAINTS

-- Source
ALTER TABLE temp.source
    ADD PRIMARY KEY (sourceid);

-- Discussion
ALTER TABLE temp.discussion
    ADD PRIMARY KEY (did);

ALTER TABLE temp.discussion
    ADD CONSTRAINT discussion_fk_source FOREIGN KEY (sourceid) REFERENCES temp.source MATCH FULL;

-- Premise
ALTER TABLE temp.premise
    ADD PRIMARY KEY (pid);

ALTER TABLE temp.premise
    ADD CONSTRAINT premise_fk_discussion FOREIGN KEY (did) REFERENCES temp.discussion MATCH FULL ON DELETE CASCADE;


-- Argument
ALTER TABLE temp.argument
    ADD PRIMARY KEY (argid);

ALTER TABLE temp.argument
    ADD CONSTRAINT argument_fk_premise FOREIGN KEY (pid) REFERENCES temp.premise MATCH FULL ON DELETE CASCADE;

-- After constraints are set we can easily delete null columns
DELETE
FROM temp.premise
WHERE did IS null;

INSERT INTO source TABLE temp.source;
INSERT INTO discussion TABLE temp.discussion;
INSERT INTO premise TABLE temp.premise;
INSERT INTO argument TABLE temp.argument;

DROP SCHEMA temp cascade;


