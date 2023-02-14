package sebfisch.store;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExceptionAwareThreadPool extends ThreadPoolExecutor {

  private Throwable anyThrownException;

  public ExceptionAwareThreadPool(int size) {
    super(size, size, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
  }

  @Override
  protected void afterExecute(Runnable r, Throwable t) {
    if (t != null) {
      synchronized (this) {
        anyThrownException = t;
      }
    }
  }

  public synchronized Throwable anyThrownException() {
    return anyThrownException;
  }
}
