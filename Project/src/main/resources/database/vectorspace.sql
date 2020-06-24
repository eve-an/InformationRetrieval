-- TODO: Put table somewhere else but not here
-- TODO: Speed up!

DROP TABLE IF EXISTS retrieved_documents;
CREATE TABLE retrieved_documents
(
    docId   INT,
    rank    numeric CHECK (rank >= 0) NOT NULL,
    docType documenttype              NOT NULL,
    PRIMARY KEY (docId, docType)
);

CREATE OR REPLACE FUNCTION insertWeights(size int, idArray int[], weightArray numeric[])
    RETURNS numeric[] AS
$$
DECLARE
    vector numeric[];
    i      int;
BEGIN
    vector = array_fill(0.0, ARRAY [size]);
    if idArray[1] IS NULL then return vector; end if;

    FOR i IN 1..array_upper(idArray, 1)
        loop
            vector[idArray[i]] = weightArray[i];
        end loop;

    return vector;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION queryToVector(query text[], size int)
    RETURNS numeric[] AS
$$
DECLARE
    term   text;
    vector numeric[];
    id     int;

BEGIN
    vector = array_fill(0.0, ARRAY [size]);
    FOREACH term IN ARRAY query
        LOOP
            SELECT tid FROM token WHERE token = term INTO id; -- Token id = Position in vector
            if id IS NOT NULL THEN
                vector[id] = 0.5; -- Maybe other weight?
            end if;
        end loop;

    return vector;
end;
$$ language plpgsql;

CREATE OR REPLACE FUNCTION dotProduct(lhs numeric[], rhs numeric[])
    RETURNS numeric AS
$$
DECLARE
    sum numeric;
BEGIN
    sum = 0;
    for i in 1..array_upper(lhs, 1)
        loop
            sum = sum + lhs[i] * rhs[i];
        end loop;

    return sum;
end;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION norm(vector numeric[])
    RETURNS numeric AS
$$
DECLARE
    sum    numeric;
    weight numeric;
BEGIN
    sum = 0.0;
    FOREACH weight in array vector
        loop
            sum = sum + weight * weight;
        end loop;
    return sqrt(sum);
end;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION similarity(lhs numeric[], rhs numeric[])
    RETURNS numeric AS
$$
DECLARE
    sim     numeric;
    lhsNorm numeric;
    rhsNorm numeric;
BEGIN
    lhsNorm = norm(lhs);
    rhsNorm = norm(rhs);

    if lhsNorm = 0 OR rhsNorm = 0 then
        return 0.0;
    end if;

    sim = dotProduct(lhs, rhs) / (norm(lhs) * norm(rhs));
    return sim;
end;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE vectorRetrieval(queryArray varchar[],
                                            minRank double precision,
                                            forTable varchar, -- e.g. argument
                                            tableIdName varchar, -- id = argid
                                            tableIndexName varchar, -- argument_index
                                            tableIndexIdName varchar) -- index-id = argid
AS
$$
DECLARE
    curs         refcursor;
    docId        INT;
    tokenIdArray Int[];
    weightArray  numeric[];
    currentVec   numeric[]; -- Vector as Array to store weights
    tokenSize    INT; -- Size of Vector
    queryVector  numeric[];
    sim          numeric; -- Cosine similarity of two vectors

BEGIN
    SELECT count(*) INTO tokenSize FROM token;

    -- docId | Array of Tokens | Array of Weights of tokens
    OPEN curs FOR EXECUTE FORMAT('SELECT doc.%s, array_agg(tid), array_agg(weight)
        FROM %s AS doc
                 LEFT JOIN %s index on doc.%s = index.%s
        GROUP BY doc.%s
        ORDER BY doc.%s', tableIdName, forTable, tableIndexName, tableIdName, tableIndexIdName,
                                 tableIdName, tableIdName);

    queryVector = queryToVector(queryArray, tokenSize);
    LOOP
        FETCH curs INTO docId, tokenIdArray, weightArray;
        EXIT WHEN NOT FOUND;

        currentVec = insertWeights(tokenSize, tokenIdArray, weightArray);
        sim = similarity(queryVector, currentVec);

        if sim > minRank then
            -- RAISE NOTICE 'Added Document % !', docId;
            INSERT INTO retrieved_documents VALUES (docId, sim, forTable::documenttype);
        end if;

    END LOOP;
    CLOSE curs;
END ;
$$ LANGUAGE plpgsql;
