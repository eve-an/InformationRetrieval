package argssearch.io;

import argssearch.shared.db.ArgDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A Utility-Class to coordinate the insertion of the json-Data to the database.
 */
class JsonDbUtil {

    private static final Logger logger = LoggerFactory.getLogger(JsonDbUtil.class);
    private PreparedStatement insertSource;
    private PreparedStatement insertDiscussion;
    private PreparedStatement insertPremise;
    private PreparedStatement insertArgument;

    private int batchCounter = 0;

    public JsonDbUtil() {
        initStatements();
    }

    private void initStatements() {
        insertSource = ArgDB.getInstance().prepareStatement("INSERT INTO temp.source (domain) VALUES (?)");
        insertDiscussion = ArgDB.getInstance().prepareStatement(
                "INSERT INTO temp.discussion (crawlid, title, url, length) VALUES (?,?,?,?)");
        insertPremise = ArgDB.getInstance()
                .prepareStatement("INSERT INTO temp.premise (crawlid, title, length) VALUES (?,?,?)");
        insertArgument = ArgDB.getInstance().prepareStatement(
                "INSERT INTO temp.argument (crawlid, content, ispro, totaltokens, length) VALUES (?,?,?,0,?)");
    }

    public void save(DBSource source) {
        try {
            insertSource.setString(1, source.getDomain());
            insertSource.executeUpdate();
        } catch (SQLException throwables) {
            if (throwables.getSQLState().equals("23505")) {  // Duplicate Key Error Code
                return;
            }
            throw new RuntimeException(throwables.getMessage());
        }
    }

    public void save(DBDiscussion discussion) {
        try {
            insertDiscussion.setString(1, discussion.getCrawlId());
            insertDiscussion.setString(2, discussion.getTitle());
            insertDiscussion.setString(3, discussion.getUrl());
            insertDiscussion.setInt(4, discussion.getLength());
            insertDiscussion.addBatch();
            batchCounter++;
        } catch (SQLException throwables) {
            throw new RuntimeException(throwables.getMessage());
        }

        // From time to time we want our data to be saved in the database
        if (batchCounter == 1000) {
            batchCounter = 0;
            logger.debug("Add 1000 Discussions to Database.");
            execBatch();
        }
    }

    public void save(DBPremise premise, DBArgument argument) {
        try {
            // Premise title and Argument content are switched, also their lengths
            insertPremise.setString(1, premise.getCrawlId());
            insertPremise.setString(2, argument.getContent());
            insertPremise.setInt(3, argument.getLength());
            insertPremise.addBatch();

            insertArgument.setString(1, argument.getCrawlId());
            insertArgument.setString(2, premise.getTitle());
            insertArgument.setBoolean(3, argument.isPro());
            insertArgument.setInt(4, premise.getLength());
            insertArgument.addBatch();
        } catch (SQLException throwables) {
            throw new RuntimeException(throwables.getLocalizedMessage());
        }
    }

    public void execBatch() {
        try {
            insertDiscussion.executeBatch();
            insertPremise.executeBatch();
            insertArgument.executeBatch();
        } catch (SQLException throwables) {
            throw new RuntimeException(throwables.getLocalizedMessage());
        }
    }

    public void closeAll() {
        try {
            insertArgument.close();
            insertPremise.close();
            insertSource.close();
            insertDiscussion.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
