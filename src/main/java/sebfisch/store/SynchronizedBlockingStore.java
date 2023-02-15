package sebfisch.store;

/**
 * Implements a thread-safe store using structured synchronization.
 */
public class SynchronizedBlockingStore extends SynchronizedStore {

  /**
   * Retrieves the value stored with the given key and removes it.
   * 
   * Blocks if no value is associated with the given key.
   * 
   * @param key
   *            key to retrieve value for
   * @return
   *         value stored with given key
   */
  @Override
  synchronized public String get(String key) {
    String value = super.get(key);

    while (value == null) {
      try {
        wait();
        value = super.get(key);
      } catch (InterruptedException e) {
        // continue waiting
      }
    }

    STORE.remove(key);
    notifyAll();

    return value;
  }

  /**
   * Associates a value with a key.
   * 
   * Blocks if another value is already associated with the given key.
   * 
   * @param key
   *              key for stored value
   * @param value
   *              stored value
   */
  @Override
  synchronized public void put(String key, String value) {
    while (STORE.containsKey(key)) {
      try {
        wait();
      } catch (InterruptedException e) {
        // continue waiting
      }
    }

    super.put(key, value);
    notifyAll();
  }

}
