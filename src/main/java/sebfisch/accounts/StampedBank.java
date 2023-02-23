package sebfisch.accounts;

import java.util.concurrent.locks.StampedLock;

public class StampedBank implements Bank<StampedBank.Account> {

  @Override
  public void transfer(Account from, Account to, int amount) throws InsufficientFundsException {
    assert amount >= 0;

    if (from == to) {
      return; // or we would try to take the same lock twice
    }

    long fromStamp;
    long toStamp;
    while (true) {
      if ((fromStamp = from.lock.tryWriteLock()) != 0L) {
        try {
          if ((toStamp = to.lock.tryWriteLock()) != 0L) {
            try {
              if (from.balance < amount) {
                throw new InsufficientFundsException();
              }
              from.balance -= amount;
              to.balance += amount;
              return;
            } finally {
              to.lock.unlockWrite(toStamp);
            }
          }
        } finally {
          from.lock.unlockWrite(fromStamp);
        }
      }
    }
  }

  @Override
  public Account createAccount() {
    return new Account();
  }

  public static class Account implements Bank.Account {
    StampedLock lock = new StampedLock();

    private int balance = 0;

    @Override
    public int balance() {
      long stamp = lock.tryOptimisticRead();
      final int result = balance;

      if (lock.validate(stamp)) {
        return result;
      }

      stamp = lock.readLock();
      try {
        return balance;
      } finally {
        lock.unlockRead(stamp);
      }
    }

    @Override
    public void deposit(int amount) {
      assert amount >= 0;
      long stamp = lock.writeLock();
      try {
        balance += amount;
      } finally {
        lock.unlockWrite(stamp);
      }
    }

    @Override
    public void withdraw(int amount) throws InsufficientFundsException {
      assert amount >= 0;
      long stamp = lock.writeLock();
      try {
        if (balance < amount) {
          throw new InsufficientFundsException();
        }
        balance -= amount;
      } finally {
        lock.unlockWrite(stamp);
      }
    }
  }

}
