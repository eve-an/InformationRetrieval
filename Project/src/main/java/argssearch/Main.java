package argssearch;


import argssearch.acquisition.Acquisition;
import argssearch.indexing.index.Indexer;
import argssearch.retrieval.models.vectorspace.DocumentTerm;
import argssearch.retrieval.models.vectorspace.VectorSpace;
import argssearch.shared.cache.TokenCachePool;
import argssearch.shared.db.ArgDB;
import argssearch.shared.nlp.CoreNlpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.Doc;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static CoreNlpService nlpService = new CoreNlpService();

    public static void main(String[] args) {
        VectorSpace vs = new VectorSpace(nlpService);
        vs.query("government police");
    }

    /**
     * Read JSONs into a database.
     * When jsonPath is a directory the whole directory will be read.
     *
     * @param jsonPath path to jsons
     */
    static void readIntoDatabase(final String jsonPath) {
        // Start with a new, clean schema
        ArgDB.getInstance().dropSchema("public");
        ArgDB.getInstance().createSchema();

        ExecutorService es = Executors.newCachedThreadPool();

        Acquisition.exec(jsonPath, new LinkedBlockingDeque<>(16), es);
    }

    /**
     * Index Documents
     */
    static void index() {
        Indexer.index(nlpService, TokenCachePool.getInstance().getDefault());
    }


}
