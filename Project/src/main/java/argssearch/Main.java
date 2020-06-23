package argssearch;

import argssearch.acquisition.Acquisition;
import argssearch.indexing.index.Indexer;
import argssearch.shared.cache.TokenCachePool;
import argssearch.shared.db.ArgDB;
import argssearch.shared.nlp.CoreNlpService;

import java.util.concurrent.LinkedBlockingDeque;

public class Main {

    private static CoreNlpService nlpService = new CoreNlpService();

    public static void main(String[] args) {
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
        Acquisition.exec(jsonPath, new LinkedBlockingDeque<>(16));
    }

    /**
     * Index Documents
     */
    static void index() {
        Indexer.index(nlpService, TokenCachePool.getInstance().getDefault());
    }


}
