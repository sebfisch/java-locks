package sebfisch.accounts;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBank implements Bank {

  protected final List<Account> accounts = new ArrayList<>();

  protected Account registeredAccount(Account account) {
    accounts.add(account);
    return account;
  }

  @Override
  public int totalFunds() {
    return accounts.stream().mapToInt(Account::balance).sum();
  }

}
