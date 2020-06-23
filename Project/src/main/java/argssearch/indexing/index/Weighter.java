package argssearch.indexing.index;

import argssearch.shared.db.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Weighter {

    private final int argumentCount;
    private final int discussionCount;
    private final int premiseCount;

    public Weighter() {
        argumentCount = ArgDB.getInstance().getRowCount("argument");
        discussionCount = ArgDB.getInstance().getRowCount("discussion");
        premiseCount = ArgDB.getInstance().getRowCount("premise");
    }

    private int getTF(String term, int id, AbstractTable table, AbstractIndexTable index) {
        Statement stmt = ArgDB.getInstance().getStatement();

        try {
            ResultSet rs = stmt.executeQuery(String.format(
                    "SELECT occurrences FROM %s tab " +
                            "LEFT JOIN %s index on tab.%s = index.%s " +
                            "LEFT JOIN token t on index.tid = t.tid " +
                            "WHERE tab.%s = %d and token = '%s'",
                    table.getTableName(),
                    index.getTableName(),
                    table.getPrimaryKeyAttributeName(),
                    index.getRefId(),
                    table.getPrimaryKeyAttributeName(),
                    id,
                    term
            ));


            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return 0;
    }

    private int getDf(String term, String counterName) {
        Statement stmt = ArgDB.getInstance().getStatement();
        try {
            ResultSet rs = stmt.executeQuery("SELECT t." + counterName + " FROM token t WHERE t.token = '" + term + "'");

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return 0;
    }

    private double getTfIdf(String token, int id, AbstractTable table, AbstractIndexTable index, String counterName) {
        double tf = getTF(token, id, table, index);
        double df = getDf(token, counterName);

        if (tf > 0) {
            return (1 + Math.log(tf)) * Math.log((double) argumentCount / df);
        } else {
            return 0;
        }
    }

    public double getDiscussionTfIdf(String token, int id) {
        return getTfIdf(token, id, new DiscussionTable(), new DiscussionIndexTable(), "discussioncounter");
    }

    public double getPremiseTfIdf(String token, int id) {
        return getTfIdf(token, id, new PremiseTable(), new PremiseIndexTable(), "premisecounter");
    }

    public double getArgumentTfIdf(String token, int id) {
        return getTfIdf(token, id, new ArgumentTable(), new ArgumentIndexTable(), "argumentcounter");
    }

    public int getArgumentCount() {
        return argumentCount;
    }

    public int getDiscussionCount() {
        return discussionCount;
    }

    public int getPremiseCount() {
        return premiseCount;
    }
}
