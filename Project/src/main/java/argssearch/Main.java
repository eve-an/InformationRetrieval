package argssearch;

import argssearch.acquisition.Acquisition;
import argssearch.indexing.index.Indexer;
import argssearch.retrieval.models.vectorspace.Document;
import argssearch.retrieval.models.vectorspace.VectorSpace;
import argssearch.shared.cache.TokenCachePool;
import argssearch.shared.db.ArgDB;
import argssearch.shared.nlp.CoreNlpService;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

public class Main {

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
    static void index(final CoreNlpService nlpService) {
        Indexer.index(nlpService, TokenCachePool.getInstance().get(Integer.MAX_VALUE));
    }

    /**
     * Retrieve relevant documents with {@link VectorSpace}-Model.
     *
     * @param query      query to process with VSM
     * @param minRank    Documents with a rank which is smaller than minRank will not be returned
     * @param nlpService to get the lemmatized query
     */
    static void queryVectorSpace(final String query, final double minRank, final CoreNlpService nlpService) {
        VectorSpace vs = new VectorSpace(nlpService);
        List<Document> results = vs.query(query, minRank);

        for (Document result : results) {
            System.out.println(result);
        }
    }


}
