package sebfisch.store;

import java.util.function.Function;

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

  @Override
  synchronized public void update(String key, Function<String, String> modification) {
    super.update(key, modification);
  }

}
