package argssearch.retrieval.models.vectorspace;

import argssearch.shared.db.ArgDB;
import argssearch.shared.nlp.CoreNlpService;
import argssearch.shared.query.Result;
import argssearch.shared.query.Result.DocumentType;
import argssearch.shared.query.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages Vector Space retrieval.
 */
public class VectorSpace {
    private static final Logger logger = LoggerFactory.getLogger(VectorSpace.class);

    private Map<String, Integer> tokenMap;
    private final Map<DocumentType, List<IndexRepresentation>> invertedIndex;
    private final Map<DocumentType, List<Result>> baseResults;

    private final CoreNlpService nlpService;
    private Vector queryVector;
    private Topic currentTopic;
    private double minRank;


    /**
     * @param nlpService {@link CoreNlpService}
     */
    public VectorSpace(CoreNlpService nlpService, double minRank) {
        this.nlpService = nlpService;
        this.minRank = minRank;
        baseResults = new HashMap<>();
        invertedIndex = new HashMap<>();
        loadTokenMap();
        loadIndexRepresentations();
    }

    /**
     * Transforms a query to a Vector. Each token has an corresponding id in the database.
     * The id is used as an index in the vector array.
     *
     * @param query query as String
     */
    private void loadQuery(final String query) {
        logger.debug("Loading Query");
        queryVector = new Vector();
        List<String> tokens = nlpService.lemmatize(query);
        for (String token : tokens) {
            Integer id = tokenMap.get(token);
            if (id != null) {
                queryVector.set(id - 1, 1);
            }
        }
    }

    /**
     * Executes a query.
     * <p>
     * Load for each new Topic a new Query Vector and a new Base Result.
     * The Base Result is the clean Vector Space query without the multipliers.
     * Each run new multipliers are multiplied to the score of the retrieved arguments.
     * <p>
     * Arguments which are retrieved from Discussions and Premises will be added to the final List.
     * Therefore we have duplicates in this list and we can group by their ids and sum up their score
     * to empathize the importance of Discussions and Premises.
     */
    public List<Result> query(final Topic topic, final double discMult,
                              final double premiseMult, final double argMult) throws SQLException {
        // Load only current query.
        // When a new Topic is loaded the base results will be cleared and loaded again.
        if (currentTopic == null || !currentTopic.getTitle().equals(topic.getTitle())) {
            logger.debug("New Query: " + topic.getTitle());
            currentTopic = topic;
            loadQuery(topic.getTitle());
            if (queryVector.isEmpty()) {
                logger.warn("Did not found any tokens to query " + topic.getTitle());
                return new ArrayList<>();
            }

            baseResults.clear();
            logger.debug("Loading Base results.");
            for (DocumentType type : DocumentType.values()) {
                baseResults.put(type, queryOverIndex(invertedIndex.get(type), type));
            }
        }

        // Copy Base results and apply multiplier
        Map<DocumentType, List<Result>> baseCopy = new HashMap<>();
        for (Map.Entry<DocumentType, List<Result>> entry : baseResults.entrySet()) {
                List<Result> clone = entry.getValue().stream()
                        .map(Result::copyResult)
                        .collect(Collectors.toList());
                baseCopy.put(entry.getKey(), clone);
        }

        baseCopy.get(DocumentType.ARGUMENT).forEach(arg -> arg.setScore(arg.getScore() * argMult));
        baseCopy.get(DocumentType.PREMISE).forEach(prem -> prem.setScore(prem.getScore() * premiseMult));
        baseCopy.get(DocumentType.DISCUSSION).forEach(disc -> disc.setScore(disc.getScore() * discMult));

        logger.debug("Retrieve Arguments");
        List<Result> results = new ArrayList<>();   // All Arguments

        // keep arguments with multiplier
        results.addAll(baseCopy.get(DocumentType.ARGUMENT));
        // multiply multiplier and retrieve arguments which belong to the discussion or premise
        results.addAll(retrieveArguments(baseCopy.get(DocumentType.PREMISE), DocumentType.PREMISE));
        results.addAll(retrieveArguments(baseCopy.get(DocumentType.DISCUSSION), DocumentType.DISCUSSION));

        // Group by Argument ids
        List<Result> args = results.stream()
                .collect(Collectors.groupingBy(Result::getDocumentId, Collectors.summingDouble(Result::getScore)))
                .entrySet()
                .stream()
                .map(entry -> new Result(DocumentType.ARGUMENT, topic.getNumber(), entry.getKey(), 0, entry.getValue()))
                .sorted()
                .collect(Collectors.toList());


        for (int i = 0; i < args.size(); i++) {
            args.get(i).setRank(i + 1);
        }

        logger.debug("Found {} arguments.", args.size());
        return args;
    }

    private List<Result> queryOverIndex(List<IndexRepresentation> indexList, DocumentType type) {
        List<Result> results = new ArrayList<>();

        indexList.forEach(index -> {
            double sim = queryVector.getCosineSimilarity(index.toVector());
            if (sim >= minRank) {
                results.add(new Result(type, currentTopic.getNumber(), index.getCrawlId(), 0, sim));
            }
        });

        return results;
    }

    /**
     * Retrieve Arguments from Premises and Discussions.
     */
    private List<Result> retrieveArguments(List<Result> results, DocumentType type) {
        List<Result> arguments = new ArrayList<>();

        for (Result result : results) {
            List<Result> args = baseResults.get(DocumentType.ARGUMENT).stream()
                    .filter(base -> {
                        String argId = base.getDocumentId();
                        if (type == DocumentType.DISCUSSION) {
                            argId = argId.substring(0, argId.indexOf("-") - 1); // Discussion-Id = First part of Argid
                        }
                        return result.getDocumentId().equals(argId);
                    })
                    .map(Result::copyResult)
                    .collect(Collectors.toList());

            args.forEach(arg -> arg.setScore(arg.getScore() * result.getScore()));
            arguments.addAll(args);
        }
        return arguments;
    }

    private List<Result> copyResults(List<Result> results) {
        List<Result> copy = new ArrayList<>();

        for (Result result : results) {
            copy.add(result.copyResult());
        }

        return copy;
    }


    private void loadTokenMap() {
        logger.debug("Loading Token map into memory.");
        // refresh materialized views when they are empty
        if (ArgDB.getInstance().getRowCount("inverted_argument_index_view") == 0 ||
                ArgDB.getInstance().getRowCount("inverted_premise_index_view") == 0 ||
                ArgDB.getInstance().getRowCount("inverted_discussion_index_view") == 0) {
            logger.debug("One of the index views is empty. A refresh must be executed.");
            ArgDB.getInstance().executeSqlFile("/database/scripts/refresh_views.sql");
        }

        tokenMap = IndexLoader.loadTokenMap();
    }

    private void loadIndexRepresentations() {
        logger.debug("Loading indexes maps into memory.");

        var argumentIndex = IndexLoader.load("inverted_argument_index_view", DocumentType.ARGUMENT);
        var premiseIndex = IndexLoader.load("inverted_premise_index_view", DocumentType.PREMISE);
        var discussionIndex = IndexLoader.load("inverted_discussion_index_view", DocumentType.DISCUSSION);

        invertedIndex.put(DocumentType.ARGUMENT, argumentIndex);
        invertedIndex.put(DocumentType.PREMISE, premiseIndex);
        invertedIndex.put(DocumentType.DISCUSSION, discussionIndex);
    }
}
