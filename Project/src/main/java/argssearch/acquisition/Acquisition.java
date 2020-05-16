package argssearch.acquisition;

import argssearch.shared.db.ArgDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class Acquisition {
    private static final Logger logger = LoggerFactory.getLogger(Acquisition.class);

    public static void exec(String path, BlockingDeque<JsonArgument> queue, ExecutorService es) {
        logger.info("Start reading Json {} and inserting data into database.", path);
        ArgDB.getInstance().executeSqlFile("/database/insertion/temp/create_temp_tables.sql");

        es.submit(new JsonProducer(path, queue));

        try {
            es.submit(new JsonConsumer(queue)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


        logger.info("Finished reading Json {}", path);
    }
}
