package sebfisch.store;

/**
 * Implements a thread-safe store using structured synchronization.
 * 
 * @param K
 *          key type
 * @param V
 *          value type
 */
public class SynchronizedStore<K, V> extends UnsafeStore<K, V> {

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
  @Override
  synchronized public V get(K key) {
    return super.get(key);
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
  @Override
  synchronized public void put(K key, V value) {
    super.put(key, value);
  }

}
