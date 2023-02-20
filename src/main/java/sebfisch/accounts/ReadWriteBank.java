package sebfisch.accounts;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteBank implements Bank<ReadWriteBank.Account> {

  @Override
  public void transfer(Account from, Account to, int amount)
      throws InsufficientFundsException {

    // Instead of fixing the order in which we take the locks,
    // we can poll them, until they are available.

    while (true) {
      if (from.lock.writeLock().tryLock()) {
        try {
          if (to.lock.writeLock().tryLock()) {
            try {
              Bank.super.transfer(from, to, amount);
              return;
            } finally {
              to.lock.writeLock().unlock();
            }
          }
        } finally {
          from.lock.writeLock().unlock();
        }
      }

      // should check the time to not poll indefinitely
    }
  }

  @Override
  public Account createAccount() {
    return new Account();
  }

  static class Account implements Bank.Account {
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private int balance;

    public Account() {
      balance = 0;
    }

    // explixit locks need to be released with try/finally

    @Override
    public int balance() {
      lock.readLock().lock();
      try {
        return balance;
      } finally {
        lock.readLock().unlock();
      }
    }

    @Override
    public void deposit(int amount) {
      assert amount >= 0;
      lock.writeLock().lock();
      try {
        balance += amount;
      } finally {
        lock.writeLock().unlock();
      }
    }

    @Override
    public void withdraw(int amount) throws InsufficientFundsException {
      assert amount >= 0;

      lock.writeLock().lock();
      try {
        if (balance < amount) {
          throw new InsufficientFundsException();
        }

        balance -= amount;
      } finally {
        lock.writeLock().unlock();
      }
    }
  }

}
