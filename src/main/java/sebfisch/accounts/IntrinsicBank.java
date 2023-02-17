package sebfisch.accounts;

public class IntrinsicBank extends AbstractBank<IntrinsicBank.Account> {

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

    if (accounts.indexOf(from) <= accounts.indexOf(to)) {
      synchronized (from) {
        synchronized (to) {
          super.transfer(from, to, amount);
        }
      }
    } else {
      synchronized (to) {
        synchronized (from) {
          super.transfer(from, to, amount);
        }
      }
    }

  }

  @Override
  public Account createAccount() {
    return registeredAccount(new Account());
  }

  static class Account implements Bank.Account {

    private int balance;

    public Account() {
      balance = 0;
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
