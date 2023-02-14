package sebfisch.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

public class StoreTests {

  void testPutGet(int id, Store store) {
    String key = randomBits(2);
    String value = randomBits(8);

    store.put(key, value);
    sleep(new Random().nextInt(100));
    assertEquals(value, store.get(key));
  }

  @Test
  void testUnsafePutGet() {
    testPutGet(1, new UnsafeStore());
  }

  void testMultiPutGet(Store store, int threadCount) throws Throwable {
    ExceptionAwareThreadPool pool = new ExceptionAwareThreadPool(threadCount);

    IntStream.range(0, 10) //
        .forEach(id -> pool.execute(() -> testPutGet(id, store)));

    pool.shutdown();
    pool.awaitTermination(1, TimeUnit.HOURS);

    assertTrue(pool.isShutdown());
    assertTrue(pool.isTerminated());

    Throwable exception = pool.anyThrownException();
    if (exception != null) {
      throw exception;
    }
  }

  @Test
  void testUnsafeSingleThreadPutGet() throws Throwable {
    testMultiPutGet(new UnsafeStore(), 1);
  }

  @Test
  void testConcurrentUnsafePutGet() {
    assertThrows( //
        AssertionFailedError.class, //
        () -> testMultiPutGet(new UnsafeStore(), 10) //
    );
  }

  @Test
  void testConcurrentSynchronizedPutGet() throws Throwable {
    assertThrows( //
        AssertionFailedError.class, //
        () -> testMultiPutGet(new SynchronizedStore(), 10) //
    );
  }

  static void sleep(int ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  static String randomBits(int length) {
    return new Random().ints(0, 2).limit(length) //
        .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append) //
        .toString();
  }

}
