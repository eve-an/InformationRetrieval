package argssearch;


import argssearch.indexing.index.Indexer;
import argssearch.indexing.index.TFIDFWeighter;
import argssearch.io.Acquisition;
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
        ArgumentParser argParser = new ArgumentParser();
        argParser
            .addStringArg("parameterRun", "pr", "Should a parameter run be performed (fromMultiplier:toMultiplier:stepSize) ie. (0:3:0.2)")
            .addStringArg("singleMultiRun", "smr" , "Should a single multi run be performed (discussionMultiplier:premiseMultiplier:argumentMultiplier) ie. (1:1:1)")
            .addStringArg("inputDirectory", "i" , "Input directory containing all files")
            .addStringArg("outputDirectory", "o", "The directory that should contain the outputs")
            .addStringArg("testDirectory", "t", "Where the run files will go")
            .addStringArg("skipReadingCrawl", "skip", "Dont read the crawled data in");

        argParser.parseArgs(args);

        ParameterRunExecutor.MultiplierRun(
            argParser.getString("inputDirectory"),
            argParser.getString("testDirectory"),
            true,
            0,
            3,
            0.1
        );
    }
}
