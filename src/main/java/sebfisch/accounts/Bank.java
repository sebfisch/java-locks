package sebfisch.accounts;

public interface Bank<A extends Bank.Account> {

  int totalFunds();

  A createAccount();

  interface Account {

    int balance();

    void deposit(int amount);

    void withdraw(int amount) throws InsufficientFundsException;

  }

  default void transfer(A from, A to, int amount) throws InsufficientFundsException {
    from.withdraw(amount);
    to.deposit(amount);
  }

}
