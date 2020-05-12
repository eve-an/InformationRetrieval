package argssearch.indexing.index;

import argssearch.shared.db.ArgDB;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Indexer {

  public static void index() {
    // one for
    new Indexer();
  }

  private Indexer() {
    indexFor("argument", "argid", "content");
    indexFor("premise", "pid", "title");
    indexFor("discussion", "did", "title");
  }

  private static void indexFor(final String tableName,
      final String primaryKeyAttributeName, final String textAttributeName) {
      PreparedStatement indexQueryStatement = ArgDB.getInstance().prepareStatement(String.format(
          "SELECT %s, %s FROM %s WHERE %s BETWEEN ? AND ?",
          primaryKeyAttributeName,
          textAttributeName,
          tableName,
          primaryKeyAttributeName
      ));

      long lowerIdBound = 1;
      long batchSize = 3;
      // TextProcessor processor;

      try {

        ResultSet result;
        while(true) {
          indexQueryStatement.setLong(1, lowerIdBound);
          indexQueryStatement.setLong(2, lowerIdBound + batchSize - 1); // -1 because between is inclusive
          result =  indexQueryStatement.executeQuery();

          long count = 0;
          for(; result.next(); count++) {

            // processor.process(indexTableName, result.getInt(1), result.getString(2));
            System.out.println(String.format("%s_index %d %s", tableName, result.getInt(1), result.getString(2)));

          }
          result.close();
          System.out.println("----------------------------");
          if (count != batchSize) {
            break;
          }
          lowerIdBound += batchSize+1;
        }

      } catch (SQLException sqlE) {
        sqlE.printStackTrace();
        if (ArgDB.isException(sqlE)) {
          sqlE.printStackTrace();
        }
      }

      //processor.finish();
  }
}
