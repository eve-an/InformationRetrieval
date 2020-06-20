package argssearch.indexing.index;

import argssearch.shared.db.ArgDB;
import java.sql.CallableStatement;
import java.sql.SQLException;

public class TFIDFWeighter {

  public static void weigh() {

    try {
      System.out.println("Start weighing the Argument-Index");
      //CallableStatement weighArgs = ArgDB.getInstance().prepareCall("{ CALL weighTFIDF(?,?,?,?) }");
      //weighArgs.setString(1, "argument_index");
      //weighArgs.setString(2, "argument");
      //weighArgs.setString(3, "argid");
      //weighArgs.setString(4, "argumentcounter");
      //weighArgs.execute();
      ArgDB.getInstance().executeNativeSql("CALL weighTFIDF('argument_index','argument','argid','argumentcounter');");
      System.out.println("Finished weighing the Argument-Index");

      System.out.println("Start weighing the Premise-Index");
      ArgDB.getInstance().executeNativeSql("CALL weighTFIDF('premise_index','premise','pid','premisecounter');");
      System.out.println("Finished weighing the Premise-Index");

      System.out.println("Start weighing the Discussion-Index");
      ArgDB.getInstance().executeNativeSql("CALL weighTFIDF('discussion_index','discussion','did','discussioncounter');");
      System.out.println("Finished weighing the Discussion-Index");
    } catch (SQLException sqlE) {
        sqlE.printStackTrace();
    }
  }
}
