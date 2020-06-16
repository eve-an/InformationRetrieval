package argssearch.indexing.index;

import argssearch.shared.db.AbstractTextTable;
import argssearch.shared.db.ArgDB;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;

class IndexTaskGatherer {


  static void gather(AbstractTextTable table, final int batchSize, final BiConsumer<Integer, String> processor) {

      // Autogenerated ids start with 1
      long lowerIdBound = 1;

      int maxId = getMaxIdForTable(table);

      try(PreparedStatement indexQueryStatement = IndexTaskGatherer.buildPreparedStatement(table)){

        ResultSet result;
        while(true) {
          indexQueryStatement.setLong(1, lowerIdBound);
          indexQueryStatement.setLong(2, lowerIdBound + batchSize - 1); // -1 because between is inclusive
          result =  indexQueryStatement.executeQuery();

          long count = 0;
          for(; result.next(); count++) {
            processor.accept(result.getInt(1), result.getString(2));
          }

          result.close();

          // adjust lower bound
          lowerIdBound += batchSize+1;
          // check if new lower bound exceeds the max id
          if (lowerIdBound > maxId) {
            break;
          }
        }
      } catch (SQLException sqlE) {
        sqlE.printStackTrace();
        if (ArgDB.isException(sqlE)) {
          sqlE.printStackTrace();
        }
      }
  }

  private static int getMaxIdForTable(final AbstractTextTable table) {
    String query = String.format(
        "SELECT MAX(%s) FROM %s",
        table.getPrimaryKeyAttributeName(),
        table.getTableName()
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

  private static PreparedStatement buildPreparedStatement(final AbstractTextTable entity) {
    return ArgDB.getInstance().prepareStatement(String.format(
        "SELECT %s, %s FROM %s WHERE %s BETWEEN ? AND ?",
        entity.getPrimaryKeyAttributeName(),
        entity.getTextAttributeName(),
        entity.getTableName(),
        entity.getPrimaryKeyAttributeName()
    ));
  }
}
