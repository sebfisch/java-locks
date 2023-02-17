package sebfisch.accounts;

import java.util.List;
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
              from.doWithdraw(amount);
              to.doDeposit(amount);
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
  public int totalFunds() {
    record LockWithStamp(StampedLock lock, long stamp) {
      static LockWithStamp fromAccountOptimistically(Account account) {
        return new LockWithStamp(account.lock, account.lock.tryOptimisticRead());
      }

      static LockWithStamp fromLockedAccount(Account account) {
        long stamp;
        while (true) {
          stamp = account.lock.tryReadLock();
          if (stamp != 0L) {
            return new LockWithStamp(account.lock, stamp);
          }
        }
      }
    }

    List<LockWithStamp> optimisticLocksWithStamp = accounts
        .stream().map(LockWithStamp::fromAccountOptimistically).toList();

    if (optimisticLocksWithStamp.stream().noneMatch(lws -> lws.stamp == 0L)) {
      // use balance attribute directly
      int result = accounts.stream().mapToInt(account -> account.balance).sum();
      if (optimisticLocksWithStamp.stream().allMatch(lws -> lws.lock.validate(lws.stamp))) {
        return result;
      }
    }

    List<LockWithStamp> lockedLocksWithStamp = accounts
        .stream().map(LockWithStamp::fromLockedAccount).toList();

    try {
      return accounts.stream().mapToInt(account -> account.balance).sum();
    } finally {
      lockedLocksWithStamp.forEach(lws -> lws.lock.unlockRead(lws.stamp));
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
        doDeposit(amount);
      } finally {
        lock.unlockWrite(stamp);
      }
    }

    void doDeposit(int amount) {
      assert amount >= 0;
      balance += amount;
    }

    @Override
    public void withdraw(int amount) throws InsufficientFundsException {
      long stamp = lock.writeLock();
      try {
        doWithdraw(amount);
      } finally {
        lock.unlockWrite(stamp);
      }
    }

    void doWithdraw(int amount) throws InsufficientFundsException {
      assert amount >= 0;

      if (balance < amount) {
        throw new InsufficientFundsException();
      }

      balance -= amount;
    }
  }

}
