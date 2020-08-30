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
    private Map<DocumentType, List<IndexRepresentation>> invertedIndex;
    private Map<DocumentType, List<Result>> baseResults;

    private final CoreNlpService nlpService;
    private Vector queryVector;
    private Topic currentTopic;
    private double minRank;


    /**
     * @param nlpService {@link CoreNlpService}
     * @param minRank
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
        queryVector = new Vector();
        List<String> tokens = nlpService.lemmatize(query);
        for (String token : tokens) {
            Integer id = tokenMap.get(token);
            if (id != null) {
                queryVector.set(id - 1, 1);
            }
        }
    }

    public static void main(String[] args) throws IOException, SQLException {
        VectorSpace vs = new VectorSpace(new CoreNlpService(), 0.4);
        vs.query(new Topic(1, "Smoking illegal", "", ""), 0.4, 1, 1, 1).forEach(System.out::println);
        vs.query(new Topic(1, "Should teachers get tenure?", "", ""), 0.4, 1, 1, 1).forEach(System.out::println);

    }

    public List<Result> query(final Topic topic, final double minRank, final double discMult,
                              final double premiseMult, final double argMult) throws SQLException {
        // Load only current query
        if (currentTopic == null || !currentTopic.getTitle().equals(topic.getTitle())) {
            currentTopic = topic;
            loadQuery(topic.getTitle());
            if (queryVector.isEmpty()) {
                logger.warn("Did not found any tokens to query " + topic.getTitle());
                return new ArrayList<>();
            }
            baseResults.clear();
            for (DocumentType type : DocumentType.values()) {
                baseResults.put(type, queryOverIndex(invertedIndex.get(type), type));
            }
        }

        List<Result> results = new ArrayList<>();
        results.addAll(addMultiplier(baseResults.get(DocumentType.ARGUMENT), argMult));
        results.addAll(retrieveArguments(addMultiplier(baseResults.get(DocumentType.PREMISE), premiseMult), DocumentType.PREMISE));
        results.addAll(retrieveArguments(addMultiplier(baseResults.get(DocumentType.DISCUSSION), discMult), DocumentType.DISCUSSION));


        Map<String, Double> argResultsWithRank = results.stream()
                .collect(Collectors.groupingBy(Result::getDocumentId, Collectors.summingDouble(Result::getScore)));

        results.forEach(result -> result.setScore(argResultsWithRank.get(result.getDocumentId())));
        Collections.sort(results);
        return results;
    }

    private List<Result> addMultiplier(List<Result> results, double multiplier) {
        List<Result> multiplied = List.copyOf(results);
        multiplied.forEach(result -> result.setScore(result.getScore() * multiplier));
        return multiplied;
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

    private List<Result> retrieveArguments(List<Result> results, DocumentType type) {
        List<Result> arguments = new ArrayList<>();

        for (Result result : results) {
            List<Result> args = baseResults.get(DocumentType.ARGUMENT).stream()
                    .filter(base -> {
                        String argId = base.getDocumentId();
                        if (type == DocumentType.DISCUSSION) {
                            argId = argId.substring(0, argId.indexOf("-") - 1);
                        }
                        return result.getDocumentId().equals(argId);
                    })
                    .collect(Collectors.toList());
            arguments.addAll(args);
        }

        return arguments;
    }


    private void loadTokenMap() {
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
        var argumentIndex = IndexLoader.load("inverted_argument_index_view", DocumentType.ARGUMENT);
        var premiseIndex = IndexLoader.load("inverted_premise_index_view", DocumentType.PREMISE);
        var discussionIndex = IndexLoader.load("inverted_discussion_index_view", DocumentType.DISCUSSION);

        invertedIndex.put(DocumentType.ARGUMENT, argumentIndex);
        invertedIndex.put(DocumentType.PREMISE, premiseIndex);
        invertedIndex.put(DocumentType.DISCUSSION, discussionIndex);
    }


    public void setMinRank(double minRank) {
        this.minRank = minRank;
    }
}
