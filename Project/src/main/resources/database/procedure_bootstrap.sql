
CREATE OR REPLACE PROCEDURE weighTFIDF(index_tableName varchar, text_tableName varchar, refAttName varchar, tokenOccAtt varchar) 
AS $$
DECLARE 
    indexCursor refcursor;

    refId INT;
    tid INT;
    tf INT;

    N DECIMAL;
    tokenOcc DECIMAL;

    totalIndexRows DECIMAL;
    rowCounter BIGINT := 0;
    progress INT;
BEGIN
    EXECUTE FORMAT('SELECT COUNT(*) FROM %s;', index_tableName) INTO totalIndexRows;
    OPEN indexCursor FOR EXECUTE FORMAT('SELECT %s, tid, occurrences AS tf FROM %s;', refAttName, index_tableName);

    LOOP
        FETCH FROM indexCursor INTO refId, tid, tf;
        EXIT WHEN refId IS NULL;

        EXECUTE FORMAT('SELECT COUNT(*) FROM %s', text_tableName) INTO N;
        EXECUTE FORMAT('SELECT %s FROM token WHERE tid=%s', tokenOccAtt, tid) INTO tokenOcc;

        EXECUTE FORMAT('UPDATE %s SET weight=%s WHERE %s=%s AND tid=%s;', index_tableName, tf*(ln(N/tokenOcc)), refAttName, refId, tid);

        IF totalIndexRows >= 10 AND rowCounter % (totalIndexRows / 10) = 0 THEN
            RAISE NOTICE 'indexed %/% of tabel %)', rowCounter, totalIndexRows, index_tableName;
        END IF;
        rowCounter := rowCounter + 1;
    END LOOP;

    CLOSE indexCursor;
END;
$$
LANGUAGE plpgsql;