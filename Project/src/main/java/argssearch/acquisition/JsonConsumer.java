package argssearch.acquisition;

import argssearch.shared.db.ArgDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;

class JsonConsumer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(JsonConsumer.class);
    private final BlockingDeque<JsonArgument> queue;
    private final JsonDbUtil db = new JsonDbUtil();

    public JsonConsumer(BlockingDeque<JsonArgument> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            // Get the first read element from the queue to create a source
            // and create the first reference discussion
            long now = System.currentTimeMillis();
            JsonArgument jsonArgument = queue.poll(1, TimeUnit.SECONDS);
            Source source = new Source(jsonArgument);
            db.save(source);

            Discussion currentDiscussion = new Discussion(jsonArgument);
            db.save(currentDiscussion);
            while (true) {
                jsonArgument = queue.poll(1, TimeUnit.SECONDS);

                if (jsonArgument == null) {
                    break;
                }

                Discussion discussion = new Discussion(jsonArgument);

                if (!currentDiscussion.equals(discussion)) {
                    currentDiscussion = discussion;
                    db.save(currentDiscussion);
                }

                Premise premise = new Premise(jsonArgument);
                Argument argument = new Argument(jsonArgument);

                db.save(premise, argument);

            }

            db.execBatch();
            logger.info("Insertion Time: {}", new SimpleDateFormat("mm:ss").format(new Date(System.currentTimeMillis() - now)));

            logger.info("Copying temp data to original database.");
            ArgDB.getInstance().executeSqlFile("/insertion/temp/temp_constraints.sql");


        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
