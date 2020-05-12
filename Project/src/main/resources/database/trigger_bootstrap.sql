CREATE OR REPLACE FUNCTION arg_index_increment_trig_func() RETURNS TRIGGER AS $arg_trig_inc_func$ BEGIN
    UPDATE token SET argumentCounter = argumentCounter + 1,
    totalCounter = totalCounter + NEW.occurences WHERE tID = NEW.tID;
    RETURN NEW;
END;
$arg_trig_inc_func$ LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION arg_index_decrement_trig_func() RETURNS TRIGGER AS $arg_trig_dec_func$ BEGIN
    UPDATE token SET argumentCounter = argumentCounter - 1, 
    totalCounter= totalCounter- OLD.occurences WHERE tID = OLD.tID;
    RETURN OLD;
END;
$arg_trig_dec_func$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS arg_index_inc_trig ON argument_index;
CREATE TRIGGER arg_index_inc_trig
AFTER INSERT OR UPDATE 
ON argument_index FOR EACH ROW EXECUTE PROCEDURE arg_index_increment_trig_func();

DROP TRIGGER IF EXISTS arg_index_dec_trig ON argument_index;
CREATE TRIGGER arg_index_dec_trig
BEFORE UPDATE OR DELETE 
ON argument_index FOR EACH ROW EXECUTE PROCEDURE arg_index_decrement_trig_func();


--------------------------------------------


CREATE OR REPLACE FUNCTION premise_index_increment_trig_func() RETURNS TRIGGER AS $premise_trig_inc_func$ BEGIN
    UPDATE token SET premiseCounter = premiseCounter + 1,
    totalCounter = totalCounter + NEW.occurences WHERE tID = NEW.tID;
    RETURN NEW;
END;
$premise_trig_inc_func$ LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION premise_index_decrement_trig_func() RETURNS TRIGGER AS $premise_trig_dec_func$ BEGIN
    UPDATE token SET premiseCounter = premiseCounter - 1,
    totalCounter = totalCounter - OLD.occurences WHERE tID = OLD.tID;
    RETURN OLD;
END;
$premise_trig_dec_func$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS premise_index_inc_trig ON premise_index;
CREATE TRIGGER premise_index_inc_trig
AFTER INSERT OR UPDATE 
ON premise_index 
FOR EACH ROW EXECUTE PROCEDURE premise_index_increment_trig_func();

DROP TRIGGER IF EXISTS premise_index_dec_trig ON premise_index;
CREATE TRIGGER premise_index_dec_trig
BEFORE UPDATE OR DELETE
ON premise_index 
FOR EACH ROW EXECUTE PROCEDURE premise_index_decrement_trig_func();



--------------------------------------------

CREATE OR REPLACE FUNCTION discussion_index_increment_trig_func() RETURNS TRIGGER AS $discussion_trig_inc_func$ BEGIN
    UPDATE token SET discussionCounter = discussionCounter + 1,
    totalCounter = totalCounter + NEW.occurences WHERE tID = NEW.tID;
    RETURN NEW;
END;
$discussion_trig_inc_func$ LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION discussion_index_decrement_trig_func() RETURNS TRIGGER AS $discussion_trig_dec_func$ BEGIN
    UPDATE token SET discussionCounter = discussionCounter - 1,
    totalCounter = totalCounter - OLD.occurences WHERE tID = OLD.tID;
    RETURN OLD;
END;
$discussion_trig_dec_func$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS discussion_index_inc_trig ON discussion_index;
CREATE TRIGGER discussion_index_inc_trig
AFTER INSERT OR UPDATE 
ON discussion_index 
FOR EACH ROW EXECUTE PROCEDURE discussion_index_increment_trig_func();

DROP TRIGGER IF EXISTS discussion_index_delete_trig ON discussion_index;
CREATE TRIGGER discussion_index_delete_trig
BEFORE UPDATE OR DELETE 
ON discussion_index 
FOR EACH ROW EXECUTE PROCEDURE discussion_index_decrement_trig_func();