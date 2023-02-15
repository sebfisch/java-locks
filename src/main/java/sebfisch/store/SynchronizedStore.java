package sebfisch.store;

/**
 * Implements a thread-safe store using structured synchronization.
 */
public class SynchronizedStore extends UnsafeStore {

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
  synchronized public String get(String key) {
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
  synchronized public void put(String key, String value) {
    super.put(key, value);
  }

}
