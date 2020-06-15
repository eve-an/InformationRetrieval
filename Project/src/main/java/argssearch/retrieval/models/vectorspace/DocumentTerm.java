package argssearch.retrieval.models.vectorspace;

import argssearch.indexing.index.Weighter;
import argssearch.shared.db.ArgDB;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class DocumentTerm {
    private List<Vector> documents;

    public DocumentTerm() {
        documents = new ArrayList<>();
    }


    public void addDocuments(final Weighter weighter) {
        ResultSet rs = ArgDB.getInstance().query("SELECT a.argid, array_agg(t.token) FROM argument a " +
                "LEFT JOIN argument_index ai on a.argid = ai.argid " +
                "LEFT JOIN token t on ai.tid = t.tid " +
                "GROUP BY a.argid " +
                "ORDER BY a.argid");

        try {
            while (rs.next()) {
                int argid = rs.getInt(1);
                Vector vector = new Vector(weighter.getArgumentCount(), argid);
                String[] docTerms = (String[]) rs.getArray(2).getArray();

                for (String docTerm : docTerms) {
                    int termIndex = ArgDB.getInstance().getIndexOfTerm(docTerm) - 1;
                    if (termIndex >= 0) {
                        vector.set(termIndex, weighter.getArgumentIdf(docTerm, argid));
                    }
                }

                documents.add(vector);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public List<Document> processQuery(Vector query) {
        List<Document> docs = new ArrayList<>();
        for (Vector document : documents) {
            double rank = VectorMath.getCosineSimilarity(query, document);

            if (rank > 0) {
                docs.add(new Document(document.getDocId(), rank));    // ID = Database ID!
            }
        }
        Collections.sort(docs);

        return docs;
    }

}
