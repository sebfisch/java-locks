package sebfisch.store;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements a store using a hash map.
 * Access is not synchronized.
 * 
 * @param K
 *          key type
 * @param V
 *          value type
 */
public class UnsafeStore<K, V> implements Store<K, V> {

  protected final Map<K, V> STORE = new HashMap<>();

  /**
   * Retrieves the value stored with the given key.
   * 
   * Returns null if no value is associated with the given key.
   * 
   * @param key
   *            key to retrieve value for
   * @return
   *         value stored with given key or null
   */
  public V get(K key) {
    return STORE.get(key);
  }

  /**
   * Associates a value with a key.
   * 
   * Overwrites an existing value already associated with the given key.
   * 
   * @param key
   *              key for stored value
   * @param value
   *              stored value
   */
  public void put(K key, V value) {
    STORE.put(key, value);
  }

}
