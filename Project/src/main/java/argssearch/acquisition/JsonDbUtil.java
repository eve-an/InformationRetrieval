package argssearch.acquisition;

import argssearch.shared.db.ArgDB;
import argssearch.shared.db.SqlCodes;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A Utility-Class to coordinate the insertion of the json-Data to the database.
 */
class JsonDbUtil {

    private PreparedStatement insertSource;
    private PreparedStatement insertDiscussion;
    private PreparedStatement insertPremise;
    private PreparedStatement insertArgument;

    public JsonDbUtil() {
        initStatements();
    }

    private void initStatements() {
        insertSource = ArgDB.getInstance().prepareStatement("INSERT INTO temp.source (domain) VALUES (?)");
        insertDiscussion = ArgDB.getInstance().prepareStatement(
                "INSERT INTO temp.discussion (crawlid, title, url) VALUES (?,?,?)");
        insertPremise = ArgDB.getInstance()
                .prepareStatement("INSERT INTO temp.premise (crawlid, title) VALUES (?,?)");
        insertArgument = ArgDB.getInstance().prepareStatement(
                "INSERT INTO temp.argument (crawlid, content, ispro, totaltokens) VALUES (?,?,?,0)");
    }

    public void save(Source source) {
        try {
            insertSource.setString(1, source.getDomain());
            insertSource.executeUpdate();
        } catch (SQLException throwables) {
            if (throwables.getSQLState().equals(SqlCodes.Key.duplicate)) {
                return;
            }
            throw new RuntimeException(throwables.getMessage());
        }
    }

    public void save(Discussion discussion) {
        try {
            insertDiscussion.setString(1, discussion.getCrawlid());
            insertDiscussion.setString(2, discussion.getTitle());
            insertDiscussion.setString(3, discussion.getUrl());
            insertDiscussion.addBatch();
        } catch (SQLException throwables) {
            throw new RuntimeException(throwables.getMessage());
        }
    }

    public void save(Premise premise, Argument argument) {
        try {
            insertPremise.setString(1, premise.getCrawlid());
            insertPremise.setString(2, premise.getTitle());
            insertPremise.addBatch();
        } catch (SQLException throwables) {
            throw new RuntimeException(throwables.getLocalizedMessage());
        }
        save(argument);
    }

    public void save(Argument argument) {
        try {
            insertArgument.setString(1, argument.getCrawlid());
            insertArgument.setString(2, argument.getContent());
            insertArgument.setBoolean(3, argument.getIspro());
            insertArgument.addBatch();
        } catch (SQLException throwables) {
            throw new RuntimeException(throwables.getMessage());
        }
    }

    public void execBatch() {
        try {
            insertDiscussion.executeLargeBatch();
            insertPremise.executeLargeBatch();
            insertArgument.executeLargeBatch();
        } catch (SQLException throwables) {
            throw new RuntimeException(throwables.getLocalizedMessage());
        }
    }
}
