package argssearch.indexing.index;

import argssearch.shared.db.ArgDB;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

class TokenCache {

  private static final String TOKEN_PRIMARY_KEY = "tid";
  private static final String TOKEN_TABLE_NAME = "token";
  private static final String TOKEN_IDENTIFIER_NAME = "token";

  /**
   * The store maps an unique identifier like domain or crawlID to the
   * internally used primary key value.
   * */
  private CacheMap<String, Integer> store;

  private PreparedStatement query;
  private PreparedStatement insert;

  TokenCache(final int cacheSize) {
    this.query = ArgDB
        .getInstance()
        .prepareStatement(
            String.format(
                "SELECT %s FROM %s WHERE %s = ?",
                TOKEN_PRIMARY_KEY,
                TOKEN_TABLE_NAME,
                TOKEN_IDENTIFIER_NAME)
        );
    this.insert = ArgDB
        .getInstance()
        .prepareStatementWithReturnOfId(String.format(
            "INSERT INTO %s(%s) VALUES (?)",
            TOKEN_TABLE_NAME,
            TOKEN_IDENTIFIER_NAME
        ), TOKEN_PRIMARY_KEY);

    this.store = new CacheMap<>(cacheSize);
  }

  int get(final String identifier) {
    // check if cached
    int lookup = this.store.getOrDefault(identifier, -1);
    // if not cached, look in db
    if (lookup == -1) {
      lookup = fetch(identifier);
      System.out.print("looking in db ");
      // if not in db, create
      if (lookup == -1) {
        lookup = insertAndGet(identifier);
        System.out.print(" have to create a new");
      }
      // whatever happened now we want it in the cache
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
      if (ArgDB.isException(sqlE)) {
        System.err.println(sqlE.getLocalizedMessage());
      }
    }
    return -1;
  }

  private int insertAndGet(final String identifier) {
    try {
      this.insert.setString(1, identifier);
      if (this.insert.executeUpdate() == 1) {
        ResultSet keys = this.insert.getGeneratedKeys();
        return keys.next() ? keys.getInt(1) : -1;
      }
    } catch (SQLException sqlE) {
      if (ArgDB.isException(sqlE)) {
        System.err.println(sqlE.getLocalizedMessage());
      }
    }
    return -1;
  }

  private static class CacheMap<K, V> extends LinkedHashMap<K, V> {
    private final int maxElements;

    public CacheMap(final int cacheSize) {
      // creates linked hash map that has 50 initial items rebalances when 3/4 full but
      // most importantly is ordered by access order not insertion order
      super(50, 0.75f, true);
      this.maxElements = cacheSize;
    }

    @Override
    protected boolean removeEldestEntry(Entry<K, V> eldest) {
      return this.size() > maxElements;
    }
  }
}
