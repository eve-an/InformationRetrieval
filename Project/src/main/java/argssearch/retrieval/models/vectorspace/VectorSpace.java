package argssearch.retrieval.models.vectorspace;

import argssearch.shared.db.ArgDB;
import argssearch.shared.nlp.CoreNlpService;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * Manages Vector Space retrieval.
 */
public class VectorSpace {

    /**
     * SQL queries to get the index of documents in the form of
     *  doc_id | token_ids | weights_of_tokens
     */
    // TODO: Additionally retrieve the crawlid for TREC Result
    static class Query {
        final static String argument = "SELECT argid, array_agg(tid), array_agg(weight::double precision) " +
                "FROM argument_index " +
                "GROUP BY argid";

        final static String discussion = "SELECT did, array_agg(tid), array_agg(weight::double precision) " +
                "FROM discussion_index " +
                "GROUP BY did";

        final static String premise = "SELECT pid, array_agg(tid), array_agg(weight::double precision) " +
                "FROM premise_index " +
                "GROUP BY pid";
    }

    private final CoreNlpService nlpService;
    private final int vectorSize;
    private Vector queryVector;

    /**
     * @param nlpService {@link CoreNlpService}
     */
    public VectorSpace(CoreNlpService nlpService) {
        this.nlpService = nlpService;
        vectorSize = ArgDB.getInstance().getRowCount("token");
    }

    /**
     * Transforms a query to a Vector.
     *
     * @param query query as String
     */
    private void loadQuery(final String query) {
        queryVector = new Vector(vectorSize, -1);
        nlpService.lemmatize(query).stream()
                .map(token -> ArgDB.getInstance().getIndexOfTerm(token))
                .forEach(id -> queryVector.set(id - 1, 0.5));   // Postgres starts indices with 1
    }

    // TODO: Implement the Result class instead of Document
    public List<Document> query(final String query, final double minRank) {
        loadQuery(query);

        ResultSet rArg = ArgDB.getInstance().query(Query.argument);
        ResultSet rPrem = ArgDB.getInstance().query(Query.premise);
        ResultSet rDisc = ArgDB.getInstance().query(Query.discussion);

        ExecutorService executor = Executors.newCachedThreadPool();

        List<Callable<List<Document>>> calls = new ArrayList<>();
        calls.add(() -> processResult(rArg, minRank, Document.Type.ARGUMENT));
        calls.add(() -> processResult(rPrem, minRank, Document.Type.PREMISE));
        calls.add(() -> processResult(rDisc, minRank, Document.Type.DISCUSSION));

        List<Document> results = new ArrayList<>();
        try {
            var futures = executor.invokeAll(calls);
            for (Future<List<Document>> future : futures) {
                results.addAll(future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        executor.shutdown();

        Collections.sort(results);

        return results;
    }

    private List<Document> processResult(final ResultSet rs, final double minRank, final Document.Type type) {
        List<Document> retrieved = new ArrayList<>();
        try {
            while (rs.next()) {
                final int docid = rs.getInt(1);
                Array sTokenIds = rs.getArray(2);
                Array sTokenWeights = rs.getArray(3);

                final Integer[] tokenIds = (Integer[]) sTokenIds.getArray();
                final Double[] tokenWeights = (Double[]) sTokenWeights.getArray();

                sTokenIds.free();
                sTokenWeights.free();

                if (tokenIds[0] == null) {
                    continue;
                }

                final Vector docVector = new Vector(vectorSize, docid);

                for (int i = 0; i < tokenIds.length; i++) {
                    final int id = tokenIds[i] - 1;
                    docVector.set(id, tokenWeights[i]);
                }

                double sim = VectorMath.getCosineSimilarity(docVector, queryVector);

                if (sim > minRank) {
                    retrieved.add(new Document(docid, sim, type));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return retrieved;
    }
}
