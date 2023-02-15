package sebfisch.accounts;

public interface Bank {

  int totalFunds();

  Account createAccount();

  interface Account {

    int balance();

    void deposit(int amount);

    void withdraw(int amount) throws InsufficientFundsException;

  }

  default void transfer(Account from, Account to, int amount) throws InsufficientFundsException {
    from.withdraw(amount);
    to.deposit(amount);
  }

}
