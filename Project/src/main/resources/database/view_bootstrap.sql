DROP MATERIALIZED VIEW IF EXISTS inverted_argument_index_view;
CREATE MATERIALIZED VIEW inverted_argument_index_view AS
SELECT crawlid, array_agg(tid) token_id_array, array_agg(weight::double precision) token_weight_array
FROM argument_index
         JOIN argument a on argument_index.argid = a.argid
GROUP BY crawlid
WITH DATA;

DROP MATERIALIZED VIEW IF EXISTS inverted_premise_index_view;
CREATE MATERIALIZED VIEW inverted_premise_index_view AS
SELECT crawlid, array_agg(tid) token_id_array, array_agg(weight::double precision) token_weight_array
FROM premise_index
         JOIN premise p on premise_index.pid = p.pid
GROUP BY crawlid
WITH DATA;

DROP MATERIALIZED VIEW IF EXISTS inverted_discussion_index_view;
CREATE MATERIALIZED VIEW inverted_discussion_index_view AS
SELECT crawlid, array_agg(tid) token_id_array, array_agg(weight::double precision) token_weight_array
FROM discussion_index
         JOIN discussion d on discussion_index.did = d.did
GROUP BY crawlid
WITH DATA;