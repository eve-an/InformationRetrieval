package argssearch.indexing.index;

import java.util.HashMap;
import java.util.Map;

public class TokenCacheFactory {

  private static class TokenCacheFactoryInstanceHolder {
    private static TokenCacheFactory instance;
  }
  static TokenCacheFactory getInstance() {
    if (TokenCacheFactoryInstanceHolder.instance == null) {
      TokenCacheFactoryInstanceHolder.instance = new TokenCacheFactory();
    }
    return TokenCacheFactoryInstanceHolder.instance;
  }

  private Map<Integer, TokenCache> caches;
  private TokenCacheFactory() {
    this.caches = new HashMap<>();
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
