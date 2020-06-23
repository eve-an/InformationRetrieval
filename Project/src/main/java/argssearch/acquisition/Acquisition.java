package argssearch.acquisition;

import argssearch.shared.db.ArgDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.*;

/**
 * Handles the loading of JSONs to the Database.
 */
public class Acquisition {
    private static final Logger logger = LoggerFactory.getLogger(Acquisition.class);
    private static final ExecutorService es = Executors.newCachedThreadPool();

    /**
     * The idea is to have a JsonProducer which reads the JSON files, transforms them to POJOs and put them into a Queue.
     * The JsonConsumer works in another Thread. It reads the Queue's items and save them into the database.
     *
     * @param path  path to JSON. If path is a dir the function will read all the JSONs inside this directory
     * @param queue working queue of JsonConsumer and JsonProducer
     */
    public static void exec(String path, BlockingDeque<JsonArgument> queue) {
        File file = new File(path);

        if (file.isDirectory()) {   // Read all files in directory
            try {
                Files.list(file.toPath()).forEach(json -> read(json.toString(), queue));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {    // Read a single file
            read(path, queue);
        }
    }

    private static void read(String path, BlockingDeque<JsonArgument> queue) {
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
