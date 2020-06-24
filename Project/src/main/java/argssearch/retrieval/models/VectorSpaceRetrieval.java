package argssearch.retrieval.models;

import argssearch.shared.db.ArgDB;
import argssearch.shared.nlp.CoreNlpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public class VectorSpaceRetrieval {
    private static final Logger logger = LoggerFactory.getLogger(VectorSpaceRetrieval.class);

    public static void query(final String query, final CoreNlpService nlpService) {
        long now = System.currentTimeMillis();
        ArgDB.getInstance().truncateTable("retrieved_documents");
        CallableStatement cs = null;

        try {
            Connection con = ArgDB.getInstance().getConn();
            cs = con.prepareCall("CALL vectorretrieval(?,?,?,?,?,?)");
            Array arr = con.createArrayOf("text", nlpService.lemmatize(query).toArray());

            cs.setArray(1, arr);        // Query
            cs.setDouble(2, 0.3);   // minimum rank

            logger.info("Retrieve Arguments...");
            cs.setString(3, "argument");
            cs.setString(4, "argid");
            cs.setString(5, "argument_index");
            cs.setString(6, "argid");

            cs.executeUpdate();

            logger.info("Retrieve Premise...");
            cs.setString(3, "premise");
            cs.setString(4, "pid");
            cs.setString(5, "premise_index");
            cs.setString(6, "pid");

            cs.executeUpdate();

            System.out.println("Retrieve Discussions...");
            cs.setString(3, "discussion");
            cs.setString(4, "did");
            cs.setString(5, "discussion_index");
            cs.setString(6, "did");

            cs.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if (cs != null) {
                try {
                    cs.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }
}
