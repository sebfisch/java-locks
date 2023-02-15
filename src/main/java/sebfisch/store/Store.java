package sebfisch.store;

/**
 * Interface for a key-value store storing strings.
 */
public interface Store {

  /**
   * Retrieves the value stored with the given key.
   * 
   * Behaviour if no value is stored with the given key is unspecified.
   * For example, implementations may return null or block in this case.
   * 
   * @param key
   *            key to retrieve value for
   * @return
   *         value stored with given key
   */
  String get(String key);

  /**
   * Associates a value with a key.
   * 
   * Behaviour if an existing value is already associated with the given key is
   * unspecified.
   * For example, implementations may overwrite the existing value or block in
   * this case.
   * 
   * @param key
   *              key for stored value
   * @param value
   *              stored value
   */
  void put(String key, String value);

}
