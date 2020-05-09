package argssearch.acquisition;

import argssearch.shared.db.ArgDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingDeque;

public class Acquisition {
    private static final Logger logger = LoggerFactory.getLogger(Acquisition.class);

    public static void exec(String path, BlockingDeque<JsonArgument> queue) {
        logger.info("Start reading Json and inserting data into database.");
        ArgDB.getInstance().executeSqlFile("/insertion/temp/create_temp_tables.sql");


        /* TODO:
         *  - Read multiple Jsons in parallel to fill up our queue faster
         *  - Or: Find a way to read one Json with multiple threads
         */
        new Thread(new JsonProducer(path, queue)).start();
        Thread t = new Thread(new JsonConsumer(queue));
        t.start();

        // Wait until Consumer has inserted all the data
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
