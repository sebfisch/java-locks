package sebfisch.accounts;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBank implements Bank {

  private final List<Account> accounts = new ArrayList<>();

  protected void registerAccount(Account account) {
    accounts.add(account);
  }

  @Override
  public int totalFunds() {
    return accounts.stream().mapToInt(Account::balance).sum();
  }

}
