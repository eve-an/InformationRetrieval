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

public class Indexer {

  public static void index(CoreNlpService nlpService, TokenCache tokenCache) {
    new Indexer(nlpService, tokenCache);
  }

  private Indexer(CoreNlpService nlpService, TokenCache tokenCache) {
      System.out.println("START indexing the argument table");
      ArgDB.getInstance().clearTable("argument_index");
      indexFor(nlpService, tokenCache, new ArgumentTable(), new ArgumentIndexTable(), 100);
      System.out.println("FINISHED indexing the argument table");

      System.out.println("START indexing the premise table");
      ArgDB.getInstance().clearTable("premise_index");
      indexFor(nlpService, tokenCache, new PremiseTable(), new PremiseIndexTable(), 1000);
      System.out.println("FINISHED indexing the premise table");

      System.out.println("START indexing the discussion table");
      ArgDB.getInstance().clearTable("discussion_index");
      indexFor(nlpService, tokenCache, new DiscussionTable(), new DiscussionIndexTable(), 1000);
      System.out.println("FINISHED indexing the discussion table");
  }

  private static void indexFor(CoreNlpService nlpService,
      final TokenCache tokenCache,
      final AbstractTextTable baseTable,
      final AbstractIndexTable indexTable,
      final int batchSize) {
    TextProcessor processor = new TextProcessor(indexTable, tokenCache, nlpService);
    IndexTaskGatherer.gather(baseTable, batchSize, processor::process);
    processor.finish();
  }
}
