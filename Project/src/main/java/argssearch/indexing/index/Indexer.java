package argssearch.indexing.index;


import argssearch.shared.cache.TokenCache;
import argssearch.shared.db.AbstractIndexTable;
import argssearch.shared.db.AbstractTextTable;
import argssearch.shared.db.ArgDB;
import argssearch.shared.db.ArgumentIndexTable;
import argssearch.shared.db.ArgumentTable;
import argssearch.shared.db.DiscussionIndexTable;
import argssearch.shared.db.DiscussionTable;
import argssearch.shared.db.PremiseIndexTable;
import argssearch.shared.db.PremiseTable;
import argssearch.shared.nlp.CoreNlpService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Indexer extends Thread{


  public static void index(CoreNlpService service, TokenCache cache) {
    try {
      Indexer pI = new Indexer(new PremiseIndexTable(), new PremiseTable(), cache, service, 4000, 4000);
      Indexer aI = new Indexer(new ArgumentIndexTable(), new ArgumentTable(), cache, service, 4000,  4000);
      Indexer dI = new Indexer(new DiscussionIndexTable(), new DiscussionTable(), cache, service, 4000,  4000);
      dI.start();
      dI.join();
      pI.start();
      pI.join();
      aI.start();
      aI.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private final AbstractTextTable textTable;
  private final int queryBatchSize;

  private final int maxId;
  private final TextProcessor processor;
  private boolean okay;
  private final BlockingQueue<Collection<TextTask>> taskBatchQueue;
  private final Logger logger;

  private Indexer(
      final AbstractIndexTable indexTable,
      final AbstractTextTable textTable,
      final TokenCache cache,
      final CoreNlpService service,
      final int queryBatchSize,
      final int insertBatchSize) {
    this.textTable = textTable;
    this.queryBatchSize = queryBatchSize;
    this.logger = LoggerFactory.getLogger(String.format("Indexer[%s]", textTable.getTableName()));

    // Clear the table before indexing
    ArgDB.getInstance().clearTable(indexTable.getTableName());

    // NOTE: the queue holds at most batchSize * 3 total items
    this.taskBatchQueue = new LinkedBlockingQueue<>(3);

    this.maxId = getMaxId();
    this.processor = new TextProcessor(indexTable, cache, service, insertBatchSize, taskBatchQueue);
  }

  @Override
  public synchronized void start() {
    super.start();
    this.processor.start();
    this.okay = true;
  }

  @Override
  public void run() {
    super.run();

    // if we are at 99 and the max is 100 this should still continue
    // TODO make pretty
    for (int i = 0; okay; i+=queryBatchSize+1) {
      try{
        ResultSet rs = query(i, i+queryBatchSize);
        logger.info("Queried {} from id {} to {}", textTable.getTableName(), i, Math.min(i+queryBatchSize, maxId));
        Collection<TextTask> tasks = new LinkedList<>();

        while(rs.next()) {
          tasks.add(new TextTask(rs.getInt(1), rs.getString(2)));
        }
        rs.close();

        if (!tasks.isEmpty())
          taskBatchQueue.put(tasks);
        logger.info("Added {} TextTasks to the TextProcessor", tasks.size());
      } catch (SQLException | InterruptedException e) {
        e.printStackTrace();
      }

      // if the max was overstepped in this round then stop
      if (i+queryBatchSize > maxId) {
        break;
      }
    }
    logger.info("Finished gathering tasks");
    // wait for the TextProcessor to finish


    try {
      processor.finishUp();
      processor.join();
      logger.info("Attached TextProcessor has shut down");
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    logger.info("Shutting down");
  }

  @Override
  public void interrupt() {
    super.interrupt();
    if (processor.isAlive()) {
      this.processor.interrupt();
    }
    this.okay = false;
  }

  private int getMaxId() {
    String query = String.format(
        "SELECT MAX(%s) FROM %s",
        this.textTable.getPrimaryKeyAttributeName(),
        this.textTable.getTableName()
    );

    try {
      ResultSet rs = ArgDB.getInstance().getStatement().executeQuery(query);
      if (rs.next()) {
        return rs.getInt(1);
      }
    } catch (SQLException sqlE) {
      if (ArgDB.isException(sqlE)) {
        sqlE.printStackTrace();
      }
    }
    return -1;
  }

  private ResultSet query(int from, int to) throws SQLException {
    return ArgDB.getInstance().getStatement().executeQuery(String.format(
        "SELECT %s, %s FROM %s WHERE %s BETWEEN %d AND %d",
        this.textTable.getPrimaryKeyAttributeName(),
        this.textTable.getTextAttributeName(),
        this.textTable.getTableName(),
        this.textTable.getPrimaryKeyAttributeName(),
        from,
        to
    ));
  }
}
