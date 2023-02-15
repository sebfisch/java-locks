package sebfisch.store;

/**
 * Interface for a key-value store storing strings.
 * 
 * @param K
 *          key type
 * @param V
 *          value type
 */
public interface Store<K, V> {

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
  V get(K key);

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
  void put(K key, V value);

}
