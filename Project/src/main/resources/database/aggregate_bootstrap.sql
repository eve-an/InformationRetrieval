
CREATE OR REPLACE AGGREGATE array_cat_agg(offsets SMALLINT[]) (
    STYPE = SMALLINT[],
    INITCOND = '{}',
    SFUNC = array_cat
);