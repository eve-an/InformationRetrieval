package argssearch.indexing.index;

import java.util.concurrent.ConcurrentHashMap;

public class TokenCachePool {

  private static class TokenCacheFactoryInstanceHolder {
    private static TokenCachePool instance;
  }
  static TokenCachePool getInstance() {
    if (TokenCacheFactoryInstanceHolder.instance == null) {
      TokenCacheFactoryInstanceHolder.instance = new TokenCachePool();
    }
    return TokenCacheFactoryInstanceHolder.instance;
  }

  private ConcurrentHashMap<Integer, TokenCache> caches;
  private TokenCachePool() {
    this.caches = new ConcurrentHashMap<>();
  }

  public TokenCache get(final int tokenCacheSize) {
    TokenCache cache = this.caches.get(tokenCacheSize);
    if (cache == null) {
      cache = new TokenCache(tokenCacheSize);
      this.caches.put(tokenCacheSize, cache);
    }
    return cache;
  }
}
