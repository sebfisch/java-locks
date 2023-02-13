package sebfisch.store;

/**
 * Interface for a key-value store storing strings.
 */
public interface Store {

  /**
   * Retrieves the value stored with the given key.
   * 
   * @param key
   *            key to retrieve value for
   * @return
   *         value stored with given key or null if none is stored
   */
  String get(String key);

  /**
   * Associates a value with a key.
   * Overwrites an existing value for the same key if one is stored.
   * 
   * @param key
   *              key for stored value
   * @param value
   *              stored value
   */
  void put(String key, String value);

}
