package sebfisch.accounts;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import sebfisch.accounts.Bank.Account;

public class Simulation {

  public static void main(String[] args) throws InterruptedException {
    new Simulation(new IntrinsicBank(), Executors.newCachedThreadPool(), 5000)
        .runRounds(100);
  }

  private final Bank bank;
  private final ExecutorService pool;

  private final List<Account> accounts;

  Simulation(Bank bank, ExecutorService pool, int count) {
    this.bank = bank;
    this.pool = pool;

    accounts = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      Account account = bank.createAccount();
      account.deposit(1000);
      accounts.add(account);
    }
  }

  void runRounds(int count) throws InterruptedException {
    for (int c = 1; c <= count; c++) {
      runRound(c);
    }

    pool.shutdown();
    pool.awaitTermination(1, TimeUnit.HOURS);
  }

  void runRound(int counter) {
    for (int i = 0; i < 1000; i++) {
      Account from = accounts.get(new Random().nextInt(accounts.size()));
      Account to = accounts.get(new Random().nextInt(accounts.size()));
      pool.execute(() -> {
        try {
          bank.transfer(from, to, 1);
        } catch (InsufficientFundsException e) {
          // ignore
        }
      });
    }

    pool.execute(() -> {
      System.out.println("Round %d: total funds %d".formatted(counter, bank.totalFunds()));
    });
  }
}
