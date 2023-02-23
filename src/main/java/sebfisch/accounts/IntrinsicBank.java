package sebfisch.accounts;

public class IntrinsicBank implements Bank<IntrinsicBank.Account> {

  @Override
  public void transfer(Account from, Account to, int amount)
      throws InsufficientFundsException {

    // The following implementation may deadlock
    // if another thread locks accounts in a different order,
    // for example, when transferring in the other direction.

    // synchronized (from) {
    // synchronized (to) {
    // super.transfer(from, to, amount);
    // }
    // }

    // To prevent deadlocks we lock accounts in a fixed order
    // determined by their index in the list of accounts:

    if (from.number() <= to.number()) {
      synchronized (from) {
        synchronized (to) {
          Bank.super.transfer(from, to, amount);
        }
      }
    } else {
      synchronized (to) {
        synchronized (from) {
          Bank.super.transfer(from, to, amount);
        }
      }
    }

  }

  private int nextNumber = 1;

  @Override
  public synchronized Account createAccount() {
    return new Account(nextNumber++);
  }

  static class Account implements Bank.Account {

    private final int number;

    private int balance = 0;

    public Account(int number) {
      this.number = number;
    }

    public int number() {
      return number;
    }

    @Override
    synchronized public int balance() {
      return balance;
    }

    @Override
    synchronized public void deposit(int amount) {
      assert amount >= 0;
      balance += amount;
    }

    @Override
    synchronized public void withdraw(int amount) throws InsufficientFundsException {
      assert amount >= 0;

      if (balance < amount) {
        throw new InsufficientFundsException();
      }

      balance -= amount;
    }

  }

}
