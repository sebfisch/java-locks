package sebfisch.accounts;

import java.util.concurrent.locks.StampedLock;

public class StampedBank extends AbstractBank<StampedBank.Account> {

  @Override
  public void transfer(Account from, Account to, int amount) throws InsufficientFundsException {
    assert from != to; // or we would try to take the same lock twice

    long fromStamp;
    long toStamp;
    while (true) {
      if ((fromStamp = from.lock.tryWriteLock()) != 0L) {
        try {
          if ((toStamp = to.lock.tryWriteLock()) != 0L) {
            try {
              // use unguarded versions because stamped locks are not reentrant
              from.unguardedWithdraw(amount);
              to.unguardedDeposit(amount);
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
    return registeredAccount(new Account());
  }

  public static class Account implements Bank.Account {
    StampedLock lock = new StampedLock();

    private int balance;

    public Account() {
      balance = 0;
    }

    @Override
    public int balance() {
      long stamp = lock.tryOptimisticRead();
      int result = balance;

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
      long stamp = lock.writeLock();
      try {
        unguardedDeposit(amount);
      } finally {
        lock.unlockWrite(stamp);
      }
    }

    void unguardedDeposit(int amount) {
      assert amount >= 0;
      balance += amount;
    }

    @Override
    public void withdraw(int amount) throws InsufficientFundsException {
      long stamp = lock.writeLock();
      try {
        unguardedWithdraw(amount);
      } finally {
        lock.unlockWrite(stamp);
      }
    }

    void unguardedWithdraw(int amount) throws InsufficientFundsException {
      assert amount >= 0;
      if (balance < amount) {
        throw new InsufficientFundsException();
      }
      balance -= amount;
    }
  }

}
