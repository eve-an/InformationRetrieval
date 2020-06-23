package argssearch.retrieval.models.vectorspace;

import argssearch.indexing.index.Weighter;
import argssearch.shared.db.ArgDB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Document Term Representation
 * Holds a List of Vectors which corresponds to documents.
 */
public class DocumentTermMatrix {
    private final List<Vector> documents;

    public DocumentTermMatrix() {
        documents = new ArrayList<>();
    }

    /**
     * Read all Documents into this matrix and compute their token's weights.
     *
     * @param weighter Weighter to compute the token's weight.
     */
    public void addDocuments(final Weighter weighter) {
        // docId : ARRAY {token1, token2...}
        ResultSet rs = ArgDB.getInstance().query("SELECT a.argid, array_agg(t.token) FROM argument a " +
                "LEFT JOIN argument_index ai on a.argid = ai.argid " +
                "LEFT JOIN token t on ai.tid = t.tid " +
                "GROUP BY a.argid " +
                "ORDER BY a.argid");

        try {
            while (rs.next()) {
                int argid = rs.getInt(1);
                Vector vector = new Vector(weighter.getArgumentCount() + weighter.getDiscussionCount() + weighter.getPremiseCount(), argid);
                String[] docTerms = (String[]) rs.getArray(2).getArray();

                for (String docTerm : docTerms) {
                    int termIndex = ArgDB.getInstance().getIndexOfTerm(docTerm) - 1;
                    if (termIndex >= 0) {
                        vector.set(termIndex, weighter.getArgumentTfIdf(docTerm, argid));
                    }
                }

                documents.add(vector);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Computes the similarity between the query and all documents from this matrix
     * and returns a List with ranked Documents sort after their ranking.
     *
     * @param query query Vector
     * @return List of ranked {@link Document} sort after their ranking.
     */
    public List<Document> processQuery(Vector query) {
        List<Document> docs = new ArrayList<>();
        for (Vector document : documents) {
            double similarity = VectorMath.getCosineSimilarity(query, document);

            // When similarity is less than 0 the two documents aren't similar
            if (similarity > 0) {
                docs.add(new Document(document.getDocId(), similarity));    // ID = Database ID!
            }
        }
        Collections.sort(docs); // Sort after similarity descending

        return docs;
    }

}
