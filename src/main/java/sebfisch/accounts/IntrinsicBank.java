package sebfisch.accounts;

public class IntrinsicBank extends AbstractBank {

  @Override
  public void transfer(Bank.Account from, Bank.Account to, int amount)
      throws InsufficientFundsException {

    synchronized (from) {
      synchronized (to) {
        super.transfer(from, to, amount);
      }
    }

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

  @Override
  public Account createAccount() {

    return null;
  }

}
