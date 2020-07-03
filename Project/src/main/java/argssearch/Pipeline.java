package argssearch;

import argssearch.acquisition.Acquisition;
import argssearch.indexing.index.Indexer;
import argssearch.indexing.index.TFIDFWeighter;
import argssearch.retrieval.models.ModelType;
import argssearch.retrieval.models.vectorspace.VectorSpace;
import argssearch.shared.cache.TokenCachePool;
import argssearch.shared.db.ArgDB;
import argssearch.shared.nlp.CoreNlpService;
import argssearch.shared.query.Result;
import argssearch.shared.query.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

public class Pipeline {

    private static final Logger logger = LoggerFactory.getLogger(Pipeline.class);

    private final Topic topic;                   // Query
    private final CoreNlpService nlpService;    // Language Processing

    /**
     * Use this when you have an empty Database and you want to fill it with the JSON-Data.
     *
     * @param topic         Query
     * @param pathToJsonDir path
     */
    public Pipeline(final Topic topic, final String pathToJsonDir) {
        this.topic = topic;
        nlpService = new CoreNlpService();
        readIntoDatabase(pathToJsonDir);    // Read all Jsons to Database
    }

    /**
     * Use this when your Database is already filled.
     *
     * @param topic Query
     */
    public Pipeline(Topic topic) {
        this.topic = topic;
        nlpService = new CoreNlpService();
    }

    public void exec(final ModelType model) {
        index(nlpService);      // Index all Documents
        TFIDFWeighter.weigh();  // Weight the terms with TF-IDF

        List<Result> results = new ArrayList<>();   // Retrieved Documents

        // Which Retrieval Model do you want to use?
        switch (model) {
            case PHRASE:
                break;
            case BOOL_CONJUNCTIVE:
                break;
            case BOOL_DISJUNCTIVE:
                break;
            case VECTOR_SPACE:
                results = queryVectorSpace(topic, 0.3, nlpService);
            default:
                break;
        }

        if (results.isEmpty()) {
            logger.info("Found no results for query '{}'", topic.getTitle());
            return;
        }

        for (Result result : results) { // Print the results to see what we have retrieved
            System.out.println(result);
        }

        // Todo: Process Results, e.g. write them to a file or do whatever you want
    }


    /**
     * Read JSONs into a database.
     * When jsonPath is a directory the whole directory will be read.
     *
     * @param jsonPath path to jsons
     */
    private void readIntoDatabase(final String jsonPath) {
        // Start with a new, clean schema
        ArgDB.getInstance().dropSchema("public");
        ArgDB.getInstance().createSchema();
        Acquisition.exec(jsonPath, new LinkedBlockingDeque<>(16));
    }

    /**
     * Index Documents
     */
    private void index(final CoreNlpService nlpService) {
        Indexer.index(nlpService, TokenCachePool.getInstance().get(Integer.MAX_VALUE));
        ArgDB.getInstance().executeSqlFile("/database/refresh_views.sql");
    }

    /**
     * Retrieve relevant documents with {@link VectorSpace}-Model.
     * Documents are ordered according to their score in descending order.
     *
     * @param query      query to process with VSM
     * @param minRank    Documents with a rank which is smaller than minRank will not be returned
     * @param nlpService to get the lemmatized query
     */
    private List<Result> queryVectorSpace(final Topic query, final double minRank, final CoreNlpService nlpService) {
        VectorSpace vs = new VectorSpace(nlpService);
        return vs.query(query, minRank);
    }
}
