package argssearch;


import argssearch.indexing.index.Indexer;
import argssearch.indexing.index.TFIDFWeighter;
import argssearch.io.Acquisition;
import argssearch.retrieval.models.vectorspace.IndexLoader;
import argssearch.shared.cache.TokenCachePool;
import argssearch.shared.db.ArgDB;
import argssearch.shared.nlp.CoreNlpService;
import argssearch.shared.util.ArgumentParser;
import executors.ParameterRunExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingDeque;


public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    /* TODO: Extract args; Idea:
        First Arg = JDBC URL for Docker => IMPORTANT: Credentials moved to db.properties file
        Second Arg = Query Input path
        Third Arg = Query Output path
    */
    public static void main(String[] args) throws IOException {
    }
}
