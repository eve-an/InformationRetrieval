package argssearch.acquisition;

import argssearch.shared.db.ArgDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.*;

public class Acquisition {
    private static final Logger logger = LoggerFactory.getLogger(Acquisition.class);

    public static void exec(String path, BlockingDeque<JsonArgument> queue, ExecutorService es) {
        File file = new File(path);

        if (file.isDirectory()) {   // Read all files in directory
            try {
                Files.list(file.toPath()).forEach(json -> read(json.toString(), queue, es));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {    // Read a single file
            read(path, queue, es);
        }
    }

    private static void read(String path, BlockingDeque<JsonArgument> queue, ExecutorService es) {
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
