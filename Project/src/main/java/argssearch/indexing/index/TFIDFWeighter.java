package argssearch.indexing.index;


import argssearch.shared.db.ArgDB;
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
    private PreparedStatement tokenOccurenceArg;
    private PreparedStatement tokenOccurencePremise;
    private PreparedStatement tokenOccurenceDiscussion;

    public TFIDFWeighter() {
        psArgumentIndexUpdate = ArgDB.getInstance().prepareStatement("UPDATE argument_index SET weight = ? WHERE argid = ? AND tid = ?");
        psPremiseIndexUpdate = ArgDB.getInstance().prepareStatement("UPDATE premise_index SET weight = ? WHERE pid = ? AND tid = ?");
        psDiscussionIndexUpdate = ArgDB.getInstance().prepareStatement("UPDATE discussion_index SET weight = ? WHERE did = ? AND tid = ?");

        tokenOccurenceArg = ArgDB.getInstance().prepareStatement("SELECT COUNT(*) FROM argument_index WHERE tid = ?");
        tokenOccurencePremise = ArgDB.getInstance().prepareStatement("SELECT COUNT(*) FROM premise_index WHERE tid = ?");
        tokenOccurenceDiscussion = ArgDB.getInstance().prepareStatement("SELECT COUNT(*) FROM discussion_index WHERE tid = ?");

        totalArgumentCount = ArgDB.getInstance().getRowCount("argument");
        totalDiscussionCount = ArgDB.getInstance().getRowCount("discussion");
        totalPremiseCount = ArgDB.getInstance().getRowCount("premise");
    }

    public void weigh() throws SQLException {

        // Get for each index the corresponding token ids, the length of the document and the occurrences of the token in the document
        String argIndex = "SELECT a.argid, array_agg(tid) ,array_agg(a.length), array_agg(occurrences) FROM argument_index " +
                "JOIN argument a on argument_index.argid = a.argid " +
                "group by a.argid";

        String premiseIndex = "SELECT a.pid, array_agg(tid) ,array_agg(a.length), array_agg(occurrences) FROM premise_index " +
                "JOIN premise a on premise_index.pid = a.pid " +
                "group by a.pid";

        String discussionIndex = "SELECT a.did, array_agg(tid) , array_agg(a.length), array_agg(occurrences) FROM discussion_index " +
                "JOIN discussion a on discussion_index.did = a.did " +
                "group by a.did";


        ResultSet argResult = ArgDB.getInstance().getStatement().executeQuery(argIndex);
        ResultSet premiseResult = ArgDB.getInstance().getStatement().executeQuery(premiseIndex);
        ResultSet discussionResult = ArgDB.getInstance().getStatement().executeQuery(discussionIndex);

        process(argResult, psArgumentIndexUpdate, totalArgumentCount, tokenOccurenceArg);
        process(premiseResult, psPremiseIndexUpdate, totalPremiseCount, tokenOccurencePremise);
        process(discussionResult, psDiscussionIndexUpdate, totalDiscussionCount, tokenOccurenceDiscussion);

        logger.info("done");
        psArgumentIndexUpdate.executeBatch();
    }

    public void process(ResultSet rs, PreparedStatement ps, int totalCount, PreparedStatement tokenPs) throws SQLException {
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

                int tokenOcc = getTokenOccurrences(tokenId, tokenPs);
                double idf = Math.log((double) totalCount / tokenOcc);

                ps.setDouble(1, tf * idf);
                ps.setInt(2, id);
                ps.setInt(3, tokenId);
                ps.addBatch();
            }
        }

        ps.executeBatch();
    }

    private int getTokenOccurrences(int tokenId, PreparedStatement ps) throws SQLException {
        ps.setInt(1, tokenId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        } else throw new IllegalStateException("Token id should exist. Id = " + tokenId);
    }
}
