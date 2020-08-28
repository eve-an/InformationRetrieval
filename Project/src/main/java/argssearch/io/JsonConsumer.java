package argssearch.io;

import argssearch.shared.db.ArgDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            DBSource dbSource = null;
            DBDiscussion currentDbDiscussion = null;
            while (true) {
                JsonArgument jsonArgument = queue.poll(1, TimeUnit.SECONDS);

                if (jsonArgument == null) {
                    break;
                }

                if (dbSource == null) {
                    dbSource = new DBSource(jsonArgument);
                    db.save(dbSource);
                }

                DBDiscussion dbDiscussion = new DBDiscussion(jsonArgument);

                if (!dbDiscussion.equals(currentDbDiscussion)) {
                    currentDbDiscussion = dbDiscussion;
                    db.save(currentDbDiscussion);
                }

                DBPremise dbPremise = new DBPremise(jsonArgument);
                DBArgument dbArg = new DBArgument(jsonArgument);

                db.save(dbPremise, dbArg);
            }

            db.execBatch(); // Add Discussions to DB if there are some left
            db.closeAll();
            logger.info("Copying temp data to original database.");
            ArgDB.getInstance().executeSqlFile("/database/scripts/insertion/temp/temp_constraints.sql");
            ArgDB.getInstance().dropSchema("temp");
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }
}
