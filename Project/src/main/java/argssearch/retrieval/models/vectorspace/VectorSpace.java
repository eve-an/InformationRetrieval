package argssearch.retrieval.models.vectorspace;

import argssearch.Main;
import argssearch.shared.db.ArgDB;
import argssearch.shared.nlp.CoreNlpService;
import argssearch.shared.query.Result;
import argssearch.shared.query.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Manages Vector Space retrieval.
 */
public class VectorSpace {
    private static final Logger logger = LoggerFactory.getLogger(VectorSpace.class);

    /**
     * SQL queries to get the index of documents in the form of
     * doc_id | token_ids | weights_of_tokens
     */
    static class Query {
        final static String argument = "SELECT * FROM inverted_argument_index_view";
        final static String discussion = "SELECT * FROM inverted_discussion_index_view";
        final static String premise = "SELECT * FROM inverted_premise_index_view";
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

        // refresh materialized views when they are empty
        if (ArgDB.getInstance().getRowCount("inverted_argument_index_view") == 0 ||
                ArgDB.getInstance().getRowCount("inverted_premise_index_view") == 0 ||
                ArgDB.getInstance().getRowCount("inverted_discussion_index_view") == 0) {
            ArgDB.getInstance().executeSqlFile("/database/refresh_views.sql");
        }
    }

    /**
     * Transforms a query to a Vector.
     *
     * @param query query as String
     */
    private void loadQuery(final String query) {
        queryVector = new Vector(vectorSize);
        nlpService.lemmatize(query).stream()
                .map(token -> ArgDB.getInstance().getIndexOfTerm(token))
                .forEach(id -> queryVector.set(id - 1, 1));   // Postgres starts indices with 1
    }

    public List<Result> query(final Topic topic, final double minRank) {
        logger.info("Start retrieving documents with query '{}'", topic.getTitle());
        loadQuery(topic.getTitle());

        ResultSet rArg = ArgDB.getInstance().query(Query.argument);
        ResultSet rPrem = ArgDB.getInstance().query(Query.premise);
        ResultSet rDisc = ArgDB.getInstance().query(Query.discussion);

        ExecutorService executor = Executors.newCachedThreadPool();

        List<Callable<List<Result>>> calls = new ArrayList<>();
        calls.add(() -> processResult(rArg, minRank, topic.getNumber(), Result.DocumentType.ARGUMENT));
        calls.add(() -> processResult(rPrem, minRank, topic.getNumber(), Result.DocumentType.PREMISE));
        calls.add(() -> processResult(rDisc, minRank, topic.getNumber(), Result.DocumentType.DISCUSSION));

        List<Result> results = new ArrayList<>();
        try {
            var futures = executor.invokeAll(calls);
            for (Future<List<Result>> future : futures) {
                results.addAll(future.get());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            // Ignore
        }

        executor.shutdown();

        Collections.sort(results);

        int rank = 1;
        for (Result result : results) {
            result.setRank(rank++);
        }


        logger.info("Finished retrieving. Results: {}", results.size());
        return results;
    }

    private List<Result> processResult(final ResultSet rs, final double minRank, final int topicNumber,
                                       final Result.DocumentType type) {
        List<Result> retrieved = new ArrayList<>();
        try {
            while (rs.next()) {
                final String docid = rs.getString(1);
                Array sTokenIds = rs.getArray(2);
                Array sTokenWeights = rs.getArray(3);

                final Integer[] tokenIds = (Integer[]) sTokenIds.getArray();
                final Double[] tokenWeights = (Double[]) sTokenWeights.getArray();

                sTokenIds.free();
                sTokenWeights.free();

                if (tokenIds[0] == null) {
                    continue;
                }

                final Vector docVector = new Vector(vectorSize);

                for (int i = 0; i < tokenIds.length; i++) {
                    final int id = tokenIds[i] - 1;
                    docVector.set(id, tokenWeights[i]);
                }

                double sim = VectorMath.getCosineSimilarity(docVector, queryVector);

                if (sim > minRank) {
                    retrieved.add(new Result(type, topicNumber, docid, 0, sim));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return retrieved;
    }
}
