package argssearch.retrieval.models.vectorspace;

import argssearch.shared.db.ArgDB;
import argssearch.shared.query.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexLoader {

    private static final Logger logger = LoggerFactory.getLogger(IndexLoader.class);

    public static List<IndexRepresentation> load(final String indexViewName, Result.DocumentType type) {
        String query = "SELECT * FROM " + indexViewName;
        Statement stmt = ArgDB.getInstance().getStatement();
        List<IndexRepresentation> rows = new ArrayList<>();
        int size = 0;
        try (ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                size++;
                final String docid = rs.getString(1);
                Array sTokenIds = rs.getArray(2);
                Array sTokenWeights = rs.getArray(3);

                final Integer[] tokenIds = (Integer[]) sTokenIds.getArray();
                final Double[] tokenWeights = (Double[]) sTokenWeights.getArray();

                if (tokenIds[0] == null) {
                    continue;
                }

                final IndexRepresentation index = new IndexRepresentation(docid, tokenIds, tokenWeights, type);
                rows.add(index);
                sTokenIds.free();
                sTokenWeights.free();
            }
            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        logger.debug("Loaded {} indexes from {} into memory.", size, indexViewName);
        return rows;
    }

    public static Map<String, Integer> loadTokenMap() {
        Map<String, Integer> tokenMap = new HashMap<>();
        Statement stmt = ArgDB.getInstance().getStatement();
        try (ResultSet rs = stmt.executeQuery("SELECT token, tid FROM token")) {
            while (rs.next()) {
                tokenMap.put(rs.getString(1), rs.getInt(2));
            }
            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return tokenMap;
    }
}
