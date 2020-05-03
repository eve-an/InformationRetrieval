package argssearch.shared.cache;

import argssearch.shared.db.ArgDB;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Cache {

  private static Logger logger = Logger.getLogger("Cache");

  /**
   * The store maps an unique identifier like domain or crawlID to the
   * internally used primary key value.
   * */
  private LimitedLinkedHashMap<String, Integer> store;

  private PreparedStatement query;

  public Cache(final int cacheSize, final String tableName,
      final String primaryKeyAttributeName, final String identifierAttributeName) {
    this.store = new LimitedLinkedHashMap<>(cacheSize);
    this.query = ArgDB
        .getInstance()
        .prepareStatement(
            String.format(
                "SELECT %s FROM %s WHERE %s = ?",
                primaryKeyAttributeName,
                tableName,
                identifierAttributeName)
        );
    logger.log(Level.INFO,
        String.format("Creating a new Cache of size %d for table %s with primary key attribute name %s and identifier %s",
            cacheSize,
            tableName,
            primaryKeyAttributeName,
            identifierAttributeName));
  }

  /**
   * Returns the primary key to the unique identifier of the column or
   * -1 if it is not in the cache already
   * */
  public int get(final String identifier) {
    // check if cached
    int lookup = this.store.getOrDefault(identifier, -1);
    // if not cached look in db
    if (lookup == -1) {
      lookup = fetch(identifier);
      this.store.put(identifier, lookup);
    }
    return lookup;
  }

  /**
   * Returns the ID of the primary key if it was found in the DB -1 otherwise
   * */
  private int fetch(final String identifier) {
    try {
      this.query.setString(1, identifier);
      ResultSet resultSet = this.query.executeQuery();
      return resultSet.next() ? resultSet.getInt(1) : -1;
    } catch (SQLException sqlE) {
      // TODO log this
      System.err.println(sqlE.getLocalizedMessage());
    }
    return -1;
  }

  /**
   * This is done here so that the cache does not expose unnecessary methods
   * */
  private static class LimitedLinkedHashMap<K, V> extends LinkedHashMap<K, V> {
    private final int maxElements;

    public LimitedLinkedHashMap(final int cacheSize) {
      this.maxElements = cacheSize;
    }

    @Override
    protected boolean removeEldestEntry(Entry<K, V> eldest) {
      return this.size() > maxElements;
    }
  }
}
