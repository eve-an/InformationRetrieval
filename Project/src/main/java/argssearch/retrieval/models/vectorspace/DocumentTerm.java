package argssearch.retrieval.models.vectorspace;

import argssearch.shared.db.ArgDB;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class DocumentTerm {
    private List<Vector> documents;
    private int tokenCount;

    public DocumentTerm() {
        tokenCount = getTokenCount();
        documents = new ArrayList<>();
    }


    private void addDocuments() {
        ResultSet rs = ArgDB.getInstance().query("SELECT a.argid, array_agg(t.token) FROM argument a\n" +
                "LEFT JOIN argument_index ai on a.argid = ai.argid\n" +
                "LEFT JOIN token t on ai.tid = t.tid\n" +
                "GROUP BY a.argid\n" +
                "ORDER BY a.argid");

        try {
            while (rs.next()) {
                Vector vector = new Vector(tokenCount);
                int argid = rs.getInt(1);
                String[] docTerms = (String[]) rs.getArray(2).getArray();

                for (String docTerm : docTerms) {
                    vector.set(getIndexOf(docTerm), getIDF(docTerm, argid));
                }

                documents.add(vector);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private int getTF(String term, int id) {
        PreparedStatement ps = ArgDB.getInstance().prepareStatement("SELECT * FROM argument a " +
                "LEFT JOIN argument_index ai on a.argid = ai.argid " +
                "LEFT JOIN token t on ai.tid = t.tid " +
                "WHERE a.argid = ? and token = ?");

        try {
            ps.setInt(1, id);
            ps.setString(2, term);

            ResultSet rs = ps.executeQuery();
            rs.next();

            return rs.getInt(1);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return 0;
    }

    private int getDf(String term) {
        PreparedStatement ps = ArgDB.getInstance().prepareStatement("SELECT argumentcounter FROM token WHERE token = ?");
        try {
            ps.setString(1, term);
            ResultSet rs = ps.executeQuery();
            rs.next();

            return rs.getInt(1);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return 0;
    }

    private double getIDF(String token, int id) {
        return (1 + Math.log(getTF(token, id))) * Math.log((double) tokenCount / getDf(token));
    }

    private int getTokenCount() {
        ResultSet rs = ArgDB.getInstance().query("SELECT COUNT(*) FROM token");

        try {
            rs.next();

            return rs.getInt(1);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return 0;
    }

    private int getIndexOf(String term) {
        PreparedStatement ps = ArgDB.getInstance().prepareStatement("SELECT tid FROM token WHERE token = ?");

        try {
            ps.setString(1, term);
            ResultSet rs = ps.executeQuery();
            rs.next();

            return rs.getInt(1);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return 0;
    }


}
