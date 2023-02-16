package sebfisch.accounts;

public class IntrinsicBank extends AbstractBank {

  @Override
  public void transfer(Bank.Account from, Bank.Account to, int amount)
      throws InsufficientFundsException {

    // The following implementation may deadlock
    // if another thread locks accounts in a different order,
    // for example, when transferring in the other direction.

    // synchronized(from) { synchronized(to) { super.transfer(from, to, amount); }}

    // To prevent deadlocks we can lock accounts in a fixed order
    // (for example by their index in the list of accounts)
    // which is unwieldy with intrinsic (block based) locks:

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

  // The inherited method totalFunds may see an inconsistent state
  // reading one account before and another after a transfer.
  // Declaring it synchronized (on the bank) would only help
  // if Account methods would also be synchronized on the bank
  // sacrificing lock granularity for deposits, withdrawals, and transfers.
  //
  // The alternative solution
  // (to lock all accounts individually before computing the total funds)
  // is possible but unwieldy with intrinsic (block based) synchronization:

  @Override
  public int totalFunds() {
    return lockedTotalFunds(0);
  }

  // locks all accounts recursively
  private int lockedTotalFunds(int accountIndex) {
    if (accountIndex >= accounts.size()) { // all accounts are locked
      return super.totalFunds();
    }

    synchronized (accounts.get(accountIndex)) {
      return lockedTotalFunds(accountIndex + 1);
    }
  }

  @Override
  public Bank.Account createAccount() {
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
