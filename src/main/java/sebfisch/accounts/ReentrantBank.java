package sebfisch.accounts;

import java.util.concurrent.locks.ReentrantLock;

public class ReentrantBank extends AbstractBank<ReentrantBank.Account> {

  @Override
  public void transfer(Account from, Account to, int amount)
      throws InsufficientFundsException {

    // Instead of fixing the order in which we take the locks,
    // we can poll them, until they are available.

    while (true) {
      if (from.lock.tryLock()) {
        try {
          if (to.lock.tryLock()) {
            try {
              super.transfer(from, to, amount);
              return;
            } finally {
              to.lock.unlock();
            }
          }
        } finally {
          from.lock.unlock();
        }
      }

      // should check the time to not poll indefinitely
    }
  }

  @Override
  public int totalFunds() {
    for (int i = 0; i < accounts.size(); i++) {
      while (!accounts.get(i).lock.tryLock()) {
      }
    }

    try {
      return super.totalFunds();
    } finally {
      for (int i = 0; i < accounts.size(); i++) {
        if (accounts.get(i).lock.isHeldByCurrentThread()) {
          accounts.get(i).lock.unlock();
        }
      }
    }
  }

  @Override
  public Account createAccount() {
    return registeredAccount(new Account());
  }

  static class Account implements Bank.Account {
    ReentrantLock lock = new ReentrantLock();

    private int balance;

    public Account() {
      balance = 0;
    }

    // explixit locks need to be released with try/finally

    @Override
    public int balance() {
      lock.lock();
      try {
        return balance;
      } finally {
        lock.unlock();
      }
    }

    @Override
    public void deposit(int amount) {
      assert amount >= 0;
      lock.lock();
      try {
        balance += amount;
      } finally {
        lock.unlock();
      }
    }

    @Override
    public void withdraw(int amount) throws InsufficientFundsException {
      assert amount >= 0;

      lock.lock();
      try {
        if (balance < amount) {
          throw new InsufficientFundsException();
        }

        balance -= amount;
      } finally {
        lock.unlock();
      }
    }
  }

}
