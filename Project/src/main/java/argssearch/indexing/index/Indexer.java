package argssearch.indexing.index;

import argssearch.shared.db.AbstractTextTable;
import argssearch.shared.db.ArgumentTable;
import argssearch.shared.db.DiscussionTable;
import argssearch.shared.db.PremiseTable;

public class Indexer {

  public static void index() {
    new Indexer();
  }

  private Indexer() {
    new Thread(() -> {
      indexFor(new ArgumentTable(), 100);
      System.out.println("FINISHED indexing the argument table");}
    ).start();
    new Thread(() -> {
      indexFor(new PremiseTable(), 1000);
      System.out.println("FINISHED indexing the premise table");}
    ).start();
    new Thread(() -> {
      indexFor(new DiscussionTable(), 1000);
      System.out.println("FINISHED indexing the discussion table");}
    ).start();
  }

  private void indexFor(AbstractTextTable table, final int batchSize) {
    TextProcessor processor = new TextProcessor(table);
    IndexTaskGatherer.gather(table, batchSize, processor::process);
    processor.finish();
  }
}
