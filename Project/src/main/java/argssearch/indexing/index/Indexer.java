package argssearch.indexing.index;

import argssearch.shared.db.AbstractEntity;
import argssearch.shared.db.AbstractTextEntity;
import argssearch.shared.db.ArgDB;
import argssearch.shared.db.ArgumentEntity;
import argssearch.shared.db.DiscussionEntity;
import argssearch.shared.db.PremiseEntity;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Indexer {

  public static void index() {
    indexFor(new ArgumentEntity(), 100);
    indexFor(new PremiseEntity(), 1000);
    indexFor(new DiscussionEntity(), 1000);
  }

  private static void indexFor(AbstractTextEntity entity, final int batchSize) {

      // Autogenerated ids start with 1
      long lowerIdBound = 1;

      // TextProcessor processor = new TextProcessor(entity);

      try(PreparedStatement indexQueryStatement = Indexer.buildPreparedStatement(entity)){

        ResultSet result;
        while(true) {
          indexQueryStatement.setLong(1, lowerIdBound);
          indexQueryStatement.setLong(2, lowerIdBound + batchSize - 1); // -1 because between is inclusive
          result =  indexQueryStatement.executeQuery();

          long count = 0;
          for(; result.next(); count++) {

            // processor.process(result.getInt(1), result.getString(2));
            System.out.println(String.format("%s_index %d %s", entity.getTableName(), result.getInt(1), result.getString(2)));

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

  private static PreparedStatement buildPreparedStatement(final AbstractTextEntity entity) {
    return ArgDB.getInstance().prepareStatement(String.format(
        "SELECT %s, %s FROM %s WHERE %s BETWEEN ? AND ?",
        entity.getPrimaryKeyAttributeName(),
        entity.getTextAttributeName(),
        entity.getTableName(),
        entity.getPrimaryKeyAttributeName()
    ));
  }
}
