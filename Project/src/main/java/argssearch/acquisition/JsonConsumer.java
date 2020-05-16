package argssearch.acquisition;

import argssearch.shared.db.ArgDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
            Source source = null;
            Discussion currentDiscussion = null;
            while (true) {
                JsonArgument jsonArgument = queue.poll(1, TimeUnit.SECONDS);

                if (jsonArgument == null) {
                    break;
                }

                if (source == null) {
                    source = new Source(jsonArgument);
                    db.save(source);
                }

                Discussion discussion = new Discussion(jsonArgument);

                if (!discussion.equals(currentDiscussion)) {
                    currentDiscussion = discussion;
                    db.save(currentDiscussion);
                }

                Premise premise = new Premise(jsonArgument);
                Argument argument = new Argument(jsonArgument);

                db.save(premise, argument);
            }

            db.execBatch();
            logger.info("Copying temp data to original database.");
            ArgDB.getInstance().executeSqlFile("/database/insertion/temp/temp_constraints.sql");

        } catch (InterruptedException e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }
}
