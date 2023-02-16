package sebfisch.accounts;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBank<A extends Bank.Account> implements Bank<A> {

  protected final List<A> accounts = new ArrayList<>();

  protected A registeredAccount(A account) {
    accounts.add(account);
    return account;
  }

  @Override
  public int totalFunds() {
    return accounts.stream().mapToInt(A::balance).sum();
  }

}
