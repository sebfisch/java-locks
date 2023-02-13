package sebfisch.store;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements a store using a hash map.
 * Access is not synchronized.
 */
public class UnsafeStore implements Store {

  private final Map<String, String> STORE = new HashMap<>();

  public String get(String key) {
    return STORE.get(key);
  }

  public void put(String key, String value) {
    STORE.put(key, value);
  }

}
