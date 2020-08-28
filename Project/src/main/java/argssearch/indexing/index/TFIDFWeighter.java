package argssearch.indexing.index;


import argssearch.shared.db.ArgDB;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TFIDFWeighter {

    private static final Logger logger = LoggerFactory.getLogger(TFIDFWeighter.class);
    private final PreparedStatement psArgumentIndexUpdate;
    private final PreparedStatement psDiscussionIndexUpdate;
    private final PreparedStatement psPremiseIndexUpdate;

    private final int totalArgumentCount;
    private final int totalDiscussionCount;
    private final int totalPremiseCount;

    private final int totalArgumentIndexCount;
    private final int totalPremiseIndexCount;
    private final int totalDiscussionIndexCount;

    private final String argIndex;
    private final String premiseIndex;
    private final String discussionIndex;

    private final String tokenOccurenceArg;
    private final String tokenOccurencePremise;
    private final String tokenOccurenceDiscussion;

    public TFIDFWeighter() {
        psArgumentIndexUpdate = ArgDB.getInstance().prepareStatement("UPDATE argument_index SET weight = ? WHERE argid = ? AND tid = ?");
        psPremiseIndexUpdate = ArgDB.getInstance().prepareStatement("UPDATE premise_index SET weight = ? WHERE pid = ? AND tid = ?");
        psDiscussionIndexUpdate = ArgDB.getInstance().prepareStatement("UPDATE discussion_index SET weight = ? WHERE did = ? AND tid = ?");

        tokenOccurenceArg = "SELECT tid, COUNT(*) FROM argument_index GROUP BY tid";
        tokenOccurencePremise = "SELECT tid, COUNT(*) FROM premise_index GROUP BY tid";
        tokenOccurenceDiscussion = "SELECT tid, COUNT(*) FROM discussion_index GROUP BY tid";

        this.argIndex = "SELECT a.argid, array_agg(tid), array_agg(a.length), array_agg(occurrences) FROM argument_index " +
            "JOIN argument a on argument_index.argid = a.argid " +
            "group by a.argid";
        this.premiseIndex = "SELECT a.pid, array_agg(tid) ,array_agg(a.length), array_agg(occurrences) FROM premise_index " +
            "JOIN premise a on premise_index.pid = a.pid " +
            "group by a.pid";
        this.discussionIndex  = "SELECT a.did, array_agg(tid) , array_agg(a.length), array_agg(occurrences) FROM discussion_index " +
            "JOIN discussion a on discussion_index.did = a.did " +
            "group by a.did";

        totalArgumentCount = ArgDB.getInstance().getRowCount("argument");
        totalDiscussionCount = ArgDB.getInstance().getRowCount("discussion");
        totalPremiseCount = ArgDB.getInstance().getRowCount("premise");
        totalArgumentIndexCount = ArgDB.getInstance().getRowCount("argument_index");
        totalDiscussionIndexCount = ArgDB.getInstance().getRowCount("discussion_index");
        totalPremiseIndexCount = ArgDB.getInstance().getRowCount("premise_index");
    }

    public void weigh() throws SQLException {
        // Get for each index the corresponding token ids, the length of the document and the occurrences of the token in the document
        logger.info("Start weighing the argument_index");
        ResultSet argResult = ArgDB.getInstance().getStatement().executeQuery(argIndex);
        process(argResult, psArgumentIndexUpdate, totalArgumentCount, totalArgumentIndexCount, getTokenOccurrences(tokenOccurenceArg));
        argResult.close();
        logger.info("Finished weighing the argument_index");

        logger.info("Start weighing the premise_index");
        ResultSet premiseResult = ArgDB.getInstance().getStatement().executeQuery(premiseIndex);
        process(premiseResult, psPremiseIndexUpdate, totalPremiseCount, totalPremiseIndexCount, getTokenOccurrences(tokenOccurencePremise));
        premiseResult.close();
        logger.info("Finished weighing the premise_index");

        logger.info("Start weighing the discussion_index");
        ResultSet discussionResult = ArgDB.getInstance().getStatement().executeQuery(discussionIndex);
        process(discussionResult, psDiscussionIndexUpdate, totalDiscussionCount, totalDiscussionIndexCount, getTokenOccurrences(tokenOccurenceDiscussion));
        discussionResult.close();
        logger.info("Finished weighing the discussion_index");

        logger.info("done");
        psArgumentIndexUpdate.executeBatch();
    }

    public void process(final ResultSet rs,
        final PreparedStatement ps,
        final int totalCount,
        final int indexTotalCount,
        final Map<Integer, Integer> tokenOccurrences) throws SQLException {
        logger.info("Start processing");

        final int BATCH_SIZE = 1000;
        long count = 0;

        while (rs.next()) {
            int id = rs.getInt(1);
            Integer[] tokenIds = (Integer[]) rs.getArray(2).getArray();
            Integer[] docLengths = (Integer[]) rs.getArray(3).getArray();
            Integer[] occurrencesInDocs = (Integer[]) rs.getArray(4).getArray();

            int arraySize = tokenIds.length;

            // Calculate for each index tf idf weight
            for (int i = 0; i < arraySize; i++) {
                int docOccurrence = occurrencesInDocs[i];
                int docLength = docLengths[i];
                int tokenId = tokenIds[i];

                double tf = Math.log(1 + ((double) docOccurrence / docLength));
                double idf = Math.log((double) totalCount / tokenOccurrences.get(tokenId));

                ps.setDouble(1, tf * idf);
                ps.setInt(2, id);
                ps.setInt(3, tokenId);
                ps.addBatch();

                if (++count % BATCH_SIZE == 0) {
                    ps.executeBatch();
                    logger.info("Inserted {} of {} ({}%)", count, indexTotalCount, (double)count/indexTotalCount * 100);
                }
            }
        }

        ps.executeBatch();
    }

    private Map<Integer, Integer> getTokenOccurrences(final String query) throws SQLException {
        Map<Integer, Integer> result = new HashMap<>();
        Statement s = ArgDB.getInstance().getStatement();
        ResultSet rs = s.executeQuery(query);
        while (rs.next()) {
            result.put(rs.getInt(1), rs.getInt(2));
        }
        rs.close();
        s.close();
        return result;
    }
}
