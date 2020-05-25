package argssearch.shared.cache;

import java.util.concurrent.ConcurrentHashMap;

public class TokenCachePool {

  private static class TokenCacheFactoryInstanceHolder {
    private static TokenCachePool instance;
  }


  private static final int DEFAULT_TOKEN_CACHE_SIZE = 10000;

  public static TokenCachePool getInstance() {
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

  public TokenCache getDefault() {
    return this.get(DEFAULT_TOKEN_CACHE_SIZE);
  }
}
