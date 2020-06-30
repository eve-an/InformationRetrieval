package argssearch.indexing.index;

import argssearch.shared.db.ArgDB;

public class TFIDFWeighter {

  public static void weigh() {
      System.out.println("Start weighing");
      ArgDB.getInstance().executeSqlFile("/database/weighter.sql");
      System.out.println("Finished weighing");
  }
}
