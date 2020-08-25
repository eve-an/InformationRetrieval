package argssearch.indexing.index;

import argssearch.shared.cache.TokenCache;
import argssearch.shared.db.AbstractIndexTable;
import argssearch.shared.db.ArgDB;
import argssearch.shared.nlp.CoreNlpService;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TextProcessor extends Thread {

    private TokenCache cache;
    private CoreNlpService nlpService;
    private int maxBatchSize;
    private PreparedStatement ps;

    private ArgDB argDB;
    private BlockingQueue<Collection<TextTask>> gatheredTaskQueue;
    private boolean okay;
    private int batchSize = 0;
    private Logger logger;


    TextProcessor(AbstractIndexTable indexTable, TokenCache cache, CoreNlpService nlpService, final int insertBatchSize, BlockingQueue<Collection<TextTask>> gatheredTasks) {
        this.cache = cache;
        this.nlpService = nlpService;
        this.maxBatchSize = insertBatchSize;
        this.gatheredTaskQueue = gatheredTasks;
        this.logger = LoggerFactory.getLogger(String.format("TextProcessor[%s]", indexTable.getTableName()));
        this.argDB = new ArgDB();

        ps = argDB.prepareStatement(String.format(
                "INSERT INTO %s (tID, %s, occurrences, offsets) VALUES (?,?,?,?)",
                indexTable.getTableName(),
                indexTable.getRefId()
        ));

    }

    @Override
    public synchronized void start() {
        super.start();
        this.okay = true;
    }

    @Override
    public void run() {
        while (this.okay || !this.gatheredTaskQueue.isEmpty()) {
            try {
                Collection<TextTask> tasks = this.gatheredTaskQueue.take();
                logger.info("Received {} new tasks from the taskQueue", tasks.size());
                tasks.forEach(this::process);
                logger.info("Finished processing {} tasks", tasks.size());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // TODO: Just let it execute the batch, when something goes wrong the exception will be caught
        // An Robin: Problem war, dass ich die Parliamentary.json eingelesen habe, die nur 48 Diskussionen enthält
        // Als maxBatchSize war aber irwas mit 40.000 eingestellt, somit wurde einfach gar nix gemacht
        // Ich weiß nicht ob es schlimm ist wenn man batches oftmals ausführt (kann ja sein dass nachdem man den Batch
        // einmal ausgeführt hat, die elemente darin gelöscht werden). Ansonsten wäre das in diesem Fall ja nicht schlimm,
        // da man ja einfach die Exception fängt.
        try {
            ps.executeBatch();
            batchSize = 0;
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        logger.info("Shutting down");
        this.argDB.close();
    }

    public synchronized void finishUp() {
        this.okay = false;
    }

    @Override
    public void interrupt() {
        if (!okay) return;
        super.interrupt();
        this.okay = false;
        logger.info("Interrupting");
    }

    void process(final TextTask task) {
        Map<String, List<Integer>> splittedString = getSplittedString(nlpService.lemmatize(task.getText()));

        for (Map.Entry<String, List<Integer>> token : splittedString.entrySet()) {

            Integer tId = token.getValue().remove(0);
            List<Integer> offsets = token.getValue();
            Integer[] array = offsets.toArray(new Integer[0]);

            try {
                ps.setInt(1, tId);
                ps.setInt(2, task.getId());
                ps.setInt(3, offsets.size());
                ps.setArray(4, ArgDB.getInstance().createArrayOf("SMALLINT", array));
                ps.addBatch();

                if (++batchSize > maxBatchSize) {
                    ps.executeBatch();
                    logger.info("Inserted {} items", batchSize);
                    batchSize = 0;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * to get the tokens from the text together with the tokenid and the positions within the text
     *
     * @param text the text as Array, every word is an entry
     * @return String ist the token, the first Integer is the tokenId followed by the indices where the token occurs
     * in the given String
     */
    private Map<String, List<Integer>> getSplittedString(List<String> text) {
        Map<String, List<Integer>> result = new HashMap<String, List<Integer>>();
        for (int i = 0; i < text.size(); i++) {
            if (result.containsKey(text.get(i))) {
                result.get(text.get(i)).add(i);
            } else {
                ArrayList<Integer> l = new ArrayList<Integer>();
                l.add(this.cache.get(text.get(i)));
                l.add(i);
                result.put(text.get(i), l);
            }
        }
        return result;
    }
}
