package argssearch.retrieval.models.vectorspace;

import argssearch.shared.db.ArgDB;
import argssearch.shared.nlp.CoreNlpService;
import argssearch.shared.query.Result;
import argssearch.shared.query.Result.DocumentType;
import argssearch.shared.query.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Manages Vector Space retrieval.
 */
public class VectorSpace {
    private static final Logger logger = LoggerFactory.getLogger(VectorSpace.class);
    private final Map<String, Integer> tokenMap;

    /**
     * SQL queries to get the index of documents in the form of
     * doc_id | token_ids | weights_of_tokens
     */
    static class Query {
        final static String argument = "SELECT * FROM inverted_argument_index_view";
        final static String filteredArgument = "SELECT * FROM inverted_argument_index_view WHERE crawlid IN ($)";
        final static String discussion = "SELECT * FROM inverted_discussion_index_view";
        final static String premise = "SELECT * FROM inverted_premise_index_view";

        final static String retrieveArgumentsFromDiscussion = "SELECT a.crawlid FROM discussion d " +
                "JOIN premise p on d.did = p.did " +
                "JOIN argument a on p.pid = a.pid " +
                "WHERE d.crawlid = ?;";

        final static String retrieveArgumentsFromPremise = "SELECT * FROM premise p " +
                "JOIN argument a on p.pid = a.pid " +
                "WHERE p.crawlid = ?";

        final static String retrieveArgumentsFromArgument = "SELECT * FROM argument " +
                "WHERE crawlid = ?";
    }

    private final CoreNlpService nlpService;
    private Vector queryVector;

    /**
     * @param nlpService {@link CoreNlpService}
     */
    public VectorSpace(CoreNlpService nlpService) {
        this.nlpService = nlpService;
        tokenMap = new HashMap<>();

        try {
            loadTokens();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        // refresh materialized views when they are empty
        if (ArgDB.getInstance().getRowCount("inverted_argument_index_view") == 0 ||
                ArgDB.getInstance().getRowCount("inverted_premise_index_view") == 0 ||
                ArgDB.getInstance().getRowCount("inverted_discussion_index_view") == 0) {
            logger.debug("One of the index views is empty. A refresh must be executed.");
            ArgDB.getInstance().executeSqlFile("/database/scripts/refresh_views.sql");
        }
    }

    /**
     * In memory representation of Token table
     */
    private void loadTokens() throws SQLException {
        logger.debug("Loading tokens for in memory representation.");
        Statement stmt = ArgDB.getInstance().getStatement();
        ResultSet rs = stmt.executeQuery("SELECT token, tid FROM token");

        while (rs.next()) {
            tokenMap.put(rs.getString(1), rs.getInt(2));
        }

        stmt.close();
        rs.close();
    }

    /**
     * Transforms a query to a Vector. Each token has an corresponding id in the database.
     * The id is used as an index in the vector array.
     *
     * @param query query as String
     */
    private void loadQuery(final String query) {
        queryVector = new Vector();
        List<String> tokens = nlpService.lemmatize(query);
        for (String token : tokens) {
            Integer id = tokenMap.get(token);
            if (id != null) {
                queryVector.set(id - 1, 1);
            }
        }
    }

    public List<Result> query(final Topic topic, final double minRank, final double discMult,
                              final double premiseMult, final double argMult) throws SQLException {
        logger.info("Start retrieving documents with query '{}'", topic.getTitle());
        loadQuery(topic.getTitle());

        Statement aStmt = ArgDB.getInstance().getStatement();
        Statement pStmt = ArgDB.getInstance().getStatement();
        Statement dStmt = ArgDB.getInstance().getStatement();

        ResultSet rArg = aStmt.executeQuery(Query.argument);
        ResultSet rPrem = pStmt.executeQuery(Query.premise);
        ResultSet rDisc = dStmt.executeQuery(Query.discussion);

        ExecutorService executor = Executors.newCachedThreadPool();

        List<Callable<List<Result>>> calls = new ArrayList<>();
        calls.add(() -> processResult(rArg, minRank, argMult, topic.getNumber(), Result.DocumentType.ARGUMENT));
        calls.add(() -> processResult(rPrem, minRank, premiseMult, topic.getNumber(), Result.DocumentType.PREMISE));
        calls.add(() -> processResult(rDisc, minRank, discMult, topic.getNumber(), Result.DocumentType.DISCUSSION));

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

        aStmt.close();
        pStmt.close();
        dStmt.close();

        Collections.sort(results);

        int rank = 1;
        for (Result result : results) {
            result.setRank(rank++);
            //processResult(rArg, minRank, argMult, topic.getNumber(), Result.DocumentType.ARGUMENT);
        }

        logger.info("Finished retrieving. Results: {}", results.size());
        return results;
    }

    public Map<Result, List<Result>> retrieveArgumentsFromType(
            final DocumentType type,
            final double minRank,
            final double multiplier,
            final int topicNumber,
            final List<Result> results) {
        Map<Result, List<Result>> discussionArgsMap = new HashMap<>();

        String query = "";
        switch (type) {
            case ARGUMENT:
                query = Query.retrieveArgumentsFromArgument;
                break;
            case PREMISE:
                query = Query.retrieveArgumentsFromPremise;
                break;
            case DISCUSSION:
                query = Query.retrieveArgumentsFromDiscussion;
                break;
        }

        PreparedStatement ps = ArgDB.getInstance().prepareStatement(query);
        if (results.stream().anyMatch(r -> r.getType() == type)) {
            List<Result> filteredResults = results.stream()
                    .distinct()
                    .filter(r -> r.getType() == type)
                    .collect(Collectors.toList());

            for (Result result : filteredResults) {
                try {
                    ps.setString(1, result.getDocumentId());

                    ResultSet rs = ps.executeQuery();
                    List<String> argIds = new ArrayList<>();

                    while (rs.next()) {
                        argIds.add("'" + rs.getString(1) + "'");
                    }

                    rs.close();

                    String argumentIndexQuery = Query.filteredArgument.replace("$", String.join(",", argIds));
                    Statement stmt = ArgDB.getInstance().getStatement();

                    List<Result> args = processResult(stmt.executeQuery(argumentIndexQuery), minRank, multiplier,
                            topicNumber, Result.DocumentType.ARGUMENT);
                    discussionArgsMap.put(result, args);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }

        }

        return discussionArgsMap;
    }

    private List<Result> processResult(final ResultSet rs, final double minRank, final double multiplier,
                                       final int topicNumber,
                                       final Result.DocumentType type) {
        List<Result> retrieved = new ArrayList<>();
        int printResults = 0;
        try {
            while (rs.next()) {
                if (printResults++ == 10000) {
                    printResults = 0;
                    logger.debug("Processed 10.000 Documents in " + type.name());
                }

                final String docid = rs.getString(1);
                Array sTokenIds = rs.getArray(2);
                Array sTokenWeights = rs.getArray(3);

                final Integer[] tokenIds = (Integer[]) sTokenIds.getArray();
                final Double[] tokenWeights = (Double[]) sTokenWeights.getArray();

                sTokenIds.free();
                sTokenWeights.free();

                if (tokenIds[0] == null || tokenWeights[0] == null) {
                    continue;
                }

                final Vector docVector = new Vector();
                docVector.read(tokenIds, tokenWeights);

                double sim = queryVector.getCosineSimilarity(docVector);

                if (sim > minRank) {
                    logger.debug("Retrieved Document '{}' - {}", docid, type.name());
                    retrieved.add(new Result(type, topicNumber, docid, 0, sim * multiplier));
                }
            }

            rs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return retrieved;
    }
}
