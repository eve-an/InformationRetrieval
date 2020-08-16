DROP AGGREGATE IF EXISTS array_cat_agg(offsets SMALLINT[]);
CREATE AGGREGATE array_cat_agg(offsets SMALLINT[]) (
    STYPE = SMALLINT[],
    INITCOND = '{}',
    SFUNC = array_cat
    );