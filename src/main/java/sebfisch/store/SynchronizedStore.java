package sebfisch.store;

/**
 * Implements a thread-safe store using structured synchronization.
 */
public class SynchronizedStore extends UnsafeStore {

  @Override
  synchronized public String get(String key) {
    return super.get(key);
  }

  @Override
  synchronized public void put(String key, String value) {
    super.put(key, value);
  }

}
