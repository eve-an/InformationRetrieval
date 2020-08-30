package argssearch;

import argssearch.indexing.index.Indexer;
import argssearch.indexing.index.TFIDFWeighter;
import argssearch.io.Acquisition;
import argssearch.retrieval.models.ConjunctiveRetrieval;
import argssearch.retrieval.models.ConjunctiveRetrievalOnAllTables;
import argssearch.retrieval.models.DisjunctiveRetrieval;
import argssearch.retrieval.models.DisjunctiveRetrievalOnAllTables;
import argssearch.retrieval.models.ModelType;
import argssearch.retrieval.models.PhraseRetrieval;
import argssearch.retrieval.models.PhraseRetrievalOnAllTables;
import argssearch.retrieval.models.vectorspace.VectorSpace;
import argssearch.shared.cache.TokenCache;
import argssearch.shared.cache.TokenCachePool;
import argssearch.shared.db.AbstractIndexTable;
import argssearch.shared.db.ArgDB;
import argssearch.shared.db.DiscussionIndexTable;
import argssearch.shared.db.PremiseIndexTable;
import argssearch.shared.nlp.CoreNlpService;
import argssearch.shared.query.Result;
import argssearch.shared.query.Result.DocumentType;
import argssearch.shared.query.Topic;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pipeline implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(Pipeline.class);

    private Topic topic;                   // Query
    private final CoreNlpService nlpService;    // Language Processing
    private VectorSpace vs;
    private ConjunctiveRetrievalOnAllTables conjunctiveRetrievalOnAllTable;
    private DisjunctiveRetrievalOnAllTables disjunctiveRetrievalOnAllTable;
    private PhraseRetrievalOnAllTables phraseRetrievalOnAllTables;
    /**
     * Use this when you have an empty Database and you want to fill it with the JSON-Data.
     *
     * @param topic         Query
     * @param pathToJsonDir path
     */
    public Pipeline(final Topic topic, final String pathToJsonDir, boolean skipReadingCrawl) throws IOException {
        this.topic = topic;
        nlpService = new CoreNlpService();
        vs = new VectorSpace(nlpService);
        conjunctiveRetrievalOnAllTable = new ConjunctiveRetrievalOnAllTables(nlpService, TokenCachePool.getInstance().getDefault());
        disjunctiveRetrievalOnAllTable = new DisjunctiveRetrievalOnAllTables(nlpService, TokenCachePool.getInstance().getDefault());
        phraseRetrievalOnAllTables = new PhraseRetrievalOnAllTables(nlpService, TokenCachePool.getInstance().getDefault());
        if (skipReadingCrawl) return;

        // Make sure the temp schema is empty
        ArgDB.getInstance().dropSchema("temp");
        readIntoDatabase(pathToJsonDir);    // Read all Jsons to Database
        index(nlpService);      // Index all Documents
        TFIDFWeighter weighter = new TFIDFWeighter();
        weighter.weigh();
    }

    public Pipeline(final String pathToJsonDir, boolean skipReadingCrawl) throws IOException {
        logger.debug("Creating pipeline with json-path=’{}’ and skipReadingCrawl='{}'", pathToJsonDir, skipReadingCrawl);
        nlpService = new CoreNlpService();
        vs = new VectorSpace(nlpService);
        conjunctiveRetrievalOnAllTable = new ConjunctiveRetrievalOnAllTables(nlpService, TokenCachePool.getInstance().getDefault());
        disjunctiveRetrievalOnAllTable = new DisjunctiveRetrievalOnAllTables(nlpService, TokenCachePool.getInstance().getDefault());
        phraseRetrievalOnAllTables = new PhraseRetrievalOnAllTables(nlpService, TokenCachePool.getInstance().getDefault());
        if (skipReadingCrawl) return;

        // Make sure the temp schema is empty
        ArgDB.getInstance().dropSchema("temp");
        readIntoDatabase(pathToJsonDir);    // Read all Jsons to Database
        index(nlpService);      // Index all Documents
        TFIDFWeighter weighter = new TFIDFWeighter();
        weighter.weigh();
    }

    public void setTopic(final Topic topic) {
        this.topic = topic;
    }

    public void execMulti(final ModelType model,
                          final double discussionMultiplier,
                          final double premiseMultiplier,
                          final double argumentMultiplier,
                          final Consumer<Result> resultConsumer) throws SQLException {
        logger.debug("Executing Multi with model='{}', discussion multiplier={}, premise multiplier={}, argument multiplier={}", model.name(), discussionMultiplier, premiseMultiplier, argumentMultiplier);
        List<Result> results = new ArrayList<>();   // Retrieved Documents

        // Which Retrieval Model do you want to use?
        switch (model) {
            case PHRASE:
                final List<Result> psResult = new LinkedList<>();
                phraseRetrievalOnAllTables.execute(
                        topic.getTitle(),
                        0,
                        0,
                        0,
                        discussionMultiplier,
                        premiseMultiplier,
                        argumentMultiplier,
                        100,
                        100,
                        100,
                        100,
                        (String id, Integer rank, Double weight) -> {
                            psResult.add(new Result(DocumentType.ARGUMENT, topic.getNumber(), id, rank, weight));
                        }
                );
                results = psResult;
                break;
            case BOOL_CONJUNCTIVE:
                final List<Result> bcResult = new LinkedList<>();
                conjunctiveRetrievalOnAllTable.execute(
                        topic.getTitle(),
                        0,
                        0,
                        0,
                        discussionMultiplier,
                        premiseMultiplier,
                        argumentMultiplier,
                        100,
                        100,
                        100,
                        100,
                        (String id, Integer rank, Double weight) -> {
                            bcResult.add(new Result(DocumentType.ARGUMENT, topic.getNumber(), id, rank, weight));
                        }
                );
                results = bcResult;
                break;
            case BOOL_DISJUNCTIVE:
                final List<Result> bdResult = new LinkedList<>();
                disjunctiveRetrievalOnAllTable.execute(
                        topic.getTitle(),
                        0,
                        0,
                        0,
                        discussionMultiplier,
                        premiseMultiplier,
                        argumentMultiplier,
                        100,
                        100,
                        100,
                        100,
                        (String id, Integer rank, Double weight) -> {
                            bdResult.add(new Result(DocumentType.ARGUMENT, topic.getNumber(), id, rank, weight));
                        }
                );
                results = bdResult;
                break;
            case VECTOR_SPACE:
                results = queryVectorSpace(topic,
                        0.1,
                        nlpService,
                        discussionMultiplier,
                        premiseMultiplier,
                        argumentMultiplier);
            default:
                break;
        }

        if (results.isEmpty()) {
            logger.info("Found no results for query '{}'", topic.getTitle());
            return;
        }

        // Give the results of the query to the consumer
        for (Result result : results) { // Print the results to see what we have retrieved
            resultConsumer.accept(result);
        }
    }

    public void execSingle(final ModelType model,
                           final AbstractIndexTable indexTable,
                           final Consumer<Result> resultConsumer) throws SQLException {
        logger.debug("Executing Single with model='{}' on table='{}'", model.name(), indexTable);
        List<Result> results = new ArrayList<>();   // Retrieved Documents

        // Which Retrieval Model do you want to use?
        switch (model) {
            case PHRASE:
                var pr = new PhraseRetrieval(
                        this.nlpService,
                        TokenCachePool.getInstance().getDefault(),
                        indexTable
                );
                final List<Result> psResult = new LinkedList<>();
                pr.execute(
                        topic.getTitle(),
                        0,
                        1,
                        (String id, Integer rank, Double weight) -> {
                            psResult.add(new Result(DocumentType.ARGUMENT, topic.getNumber(), id, rank, weight));
                        }
                );
                results = psResult;
                break;
            case BOOL_CONJUNCTIVE:
                var cr = new ConjunctiveRetrieval(
                        this.nlpService,
                        TokenCachePool.getInstance().getDefault(),
                        indexTable
                );
                final List<Result> crResult = new LinkedList<>();
                cr.execute(
                        topic.getTitle(),
                        0,
                        1,
                        (String id, Integer rank, Double weight) -> {
                            crResult.add(new Result(DocumentType.ARGUMENT, topic.getNumber(), id, rank, weight));
                        }
                );
                results = crResult;
                break;
            case BOOL_DISJUNCTIVE:
                var dr = new DisjunctiveRetrieval(
                        this.nlpService,
                        TokenCachePool.getInstance().getDefault(),
                        indexTable
                );
                final List<Result> drResult = new LinkedList<>();
                dr.execute(
                        topic.getTitle(),
                        0,
                        1,
                        (String id, Integer rank, Double weight) -> {
                            drResult.add(new Result(DocumentType.ARGUMENT, topic.getNumber(), id, rank, weight));
                        }
                );
                results = drResult;
                break;
            case VECTOR_SPACE:
                ArgDB.getInstance().executeSqlFile("/database/scripts/refresh_views.sql");
                results = queryVectorSpaceByTable(
                        indexTable,
                        topic,
                        0.1,
                        nlpService,
                        1,
                        1,
                        1
                );
            default:
                break;
        }

        if (results.isEmpty()) {
            logger.info("Found no results for query '{}' for model {}", topic.getTitle(), model.toString());
            return;
        }

        // Give the results of the query to the consumer
        for (Result result : results) { // Print the results to see what we have retrieved
            resultConsumer.accept(result);
        }
    }

    /**
     * Read JSONs into a database.
     * When jsonPath is a directory the whole directory will be read.
     */
    private void readIntoDatabase(final String inputDirectory) {
        logger.debug("Reading json-dir='{}' into database", inputDirectory);
        // Start with a new, clean schema
        ArgDB.getInstance().dropSchema("public");
        ArgDB.getInstance().createSchema();
        Acquisition.exec(inputDirectory, new LinkedBlockingDeque<>(16));
    }

    /**
     * Index Documents
     */
    private void index(final CoreNlpService nlpService) {
        logger.debug("Indexing tables");
        Indexer.index(nlpService, TokenCachePool.getInstance().get(Integer.MAX_VALUE));
    }

    /**
     * Retrieve relevant documents with {@link VectorSpace}-Model.
     * Documents are ordered according to their score in descending order.
     *
     * @param query      query to process with VSM
     * @param minRank    Documents with a rank which is smaller than minRank will not be returned
     * @param nlpService to get the lemmatized query
     */
    private List<Result> queryVectorSpace(final Topic query, final double minRank, final CoreNlpService nlpService,
                                          final double discMult,
                                          final double premiseMult, final double argMult) throws SQLException {
        return vs.query(query, minRank, discMult, premiseMult, argMult);
    }

    /**
     * Retrieve relevant documents with {@link VectorSpace}-Model of the specified
     * type and return their respected arguments.
     * Documents are ordered according to their score in descending order.
     *
     * @param query      query to process with VSM
     * @param minRank    Documents with a rank which is smaller than minRank will not be returned
     * @param nlpService to get the lemmatized query
     */
    private List<Result> queryVectorSpaceByTable(
            final AbstractIndexTable table,
            final Topic query,
            final double minRank,
            final CoreNlpService nlpService,
            final double discMult,
            final double premiseMult,
            final double argMult) throws SQLException {
        DocumentType t = DocumentType.ARGUMENT;
        if (table instanceof DiscussionIndexTable) t = DocumentType.DISCUSSION;
        if (table instanceof PremiseIndexTable) t = DocumentType.PREMISE;

        final DocumentType finalT = t;
        return vs.retrieveArgumentsFromType(
                t,
                minRank,
                1,
                topic.getNumber(),
                vs.query(query, minRank, discMult, premiseMult, argMult).stream()
                        .filter(r -> r.getType() == finalT)
                        .collect(Collectors.toList())
        ).values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public void close() throws Exception {
        phraseRetrievalOnAllTables.close();
        conjunctiveRetrievalOnAllTable.close();
        disjunctiveRetrievalOnAllTable.close();
    }
}
