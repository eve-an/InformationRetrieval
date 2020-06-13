-- given a tidOrder of {3, 4, 1, 3} we create 4 "pointers":
--          - two for the area in the offset array that contains the offsets for tid 3
--          - one for the area in the offset array that contains the offsets for tid 4
--          - one for the area in the offset array that contains the offsets for tid 1
--
-- Knowing that if the order is {3, 4, 1, 3} the pointers in the the area can be right next to each other
-- because there shouldn't be any other occurrence inbetween.
CREATE OR REPLACE FUNCTION phraseCount(tidOrder INT[], tids INT[], occurrences INT[], offsets SMALLINT[])
RETURNS INT
AS $$
DECLARE
    --
    offsetPointers SMALLINT[];
    indexBound INT[];
    orderOcc INT[];
    
    i INT;
    tidIndex INT;
    _offset INT;
    currOccurrenceSum INT;
    phraseMatchCount INT := 0;

    allAtMax BOOLEAN;
    hadMatch BOOLEAN;
BEGIN

    -- find the section starts of the areas
    -- create the array that will be filled by the correct pointers
    offsetPointers := array_fill(0, ARRAY[array_length(tidOrder, 1)]);
    indexBound := array_fill(0, ARRAY[array_length(tidOrder, 1)]);

    -- create the pointer for every tid
    tidIndex := 1;
    currOccurrenceSum := 0;
    LOOP
        orderOcc := array_positions(tidOrder, tids[tidIndex]);

        -- If there are less than the occurrences for that tid was in the order the result can only be 0
        if array_length(orderOcc, 1) > occurrences[tidIndex] THEN 
            RETURN 0;
        END IF;

        _offset := 1;
        LOOP
            offsetPointers[orderOcc[_offset]] := currOccurrenceSum + _offset;
            indexBound[orderOcc[_offset]] := currOccurrenceSum + occurrences[tidIndex];
            _offset := _offset +1;
            EXIT WHEN _offset > array_length(orderOcc, 1);
        END LOOP;

        currOccurrenceSum := currOccurrenceSum + occurrences[tidIndex];
        tidIndex := tidIndex+1;
        EXIT WHEN tidIndex > array_length(tids, 1);
    END LOOP;

    <<outerLoop>>
    LOOP
        allAtMax := true;
        hadMatch := true;

        i := 1;
        <<matchFindingLoop>>
        LOOP
            -- if the left one is far apart from the right one
            -- move the left one ahaed and mark that there was no 
            -- match but someone moved
            IF offsets[offsetPointers[i+1]] - offsets[offsetPointers[i]] > 1 THEN
                hadMatch := false;
                IF offsetPointers[i]+1 <= indexBound[i] THEN
                    offsetPointers[i] := offsetPointers[i]+1;
                    allAtMax := false;
                END IF; 
                EXIT matchFindingLoop;
            END IF;

            IF offsets[offsetPointers[i+1]] - offsets[offsetPointers[i]] < 1 THEN
                hadMatch := false;
                IF offsetPointers[i+1]+1 <= indexBound[i+1] THEN
                    offsetPointers[i+1] := offsetPointers[i+1] + 1;
                    allAtMax := false;
                END IF;
                EXIT matchFindingLoop;
            END IF;

            -- add labels for the exit to work
            i := i + 1;
            EXIT WHEN i >= array_length(offsetPointers, 1);
        END LOOP;


        -- if we had a match we can move all ahead
        i := 1;
        IF hadMatch IS TRUE THEN
            phraseMatchCount := phraseMatchCount +1;
            LOOP
                -- move ahead if possible and mark that someone had moved
                IF offsetPointers[i]+1 <= indexBound[i] THEN
                    offsetPointers[i] := offsetPointers[i]+1;
                    allAtMax := false;
                ELSE
                    -- if there has just been a match but one cannot move forward then
                    -- we know that it is at the end
                    --allAtMax := true;
                    EXIT outerLoop;
                END IF;

                
                i := i + 1;
                EXIT WHEN i > array_length(offsetPointers, 1);
            END LOOP;
        END IF;

        EXIT WHEN allAtMax IS true;
    END LOOP;

    RETURN phraseMatchCount;
END;
$$
LANGUAGE plpgsql;
