package sebfisch.accounts;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public class Simulation<A extends Bank.Account> {

  record Config(
      int startingBalance,
      int numberOfAccounts,
      int numberOfRounds,
      int readsPerRound,
      int writesPerRound) {
  }

  static final Config config = new Config(
      256,
      8,
      128,
      1024,
      128);

  public static void main(String[] args) throws InterruptedException {
    System.out.println(config);

    new Simulation<>(new IntrinsicBank(), Executors.newCachedThreadPool())
        .runRounds();
  }

  private final Bank<A> bank;
  private final ExecutorService pool;

  private final List<A> accounts = new ArrayList<>();
  private final LongAdder observedTotalBalance = new LongAdder();
  private final RandomGenerator rgen = RandomGeneratorFactory.getDefault().create();

  Simulation(Bank<A> bank, ExecutorService pool) {
    this.bank = bank;
    this.pool = pool;

    for (int i = 0; i < config.numberOfAccounts(); i++) {
      A account = bank.createAccount();
      account.deposit(config.startingBalance());
      accounts.add(account);
    }
  }

  A randomAccount() {
    return accounts.get(rgen.nextInt(accounts.size()));
  }

  void runRounds() throws InterruptedException {
    System.out.println("executing %s with %s..."
        .formatted(bank.getClass().getSimpleName(), pool.getClass().getSimpleName()));

    Instant start = Instant.now();
    for (int c = 1; c <= config.numberOfRounds(); c++) {
      runRound();
    }

    pool.shutdown();
    pool.awaitTermination(1, TimeUnit.HOURS); // should finish sooner unless deadlock

    long durationInMs = Duration.between(start, Instant.now()).toMillis();
    System.out.println("total balance is %d after %dms"
        .formatted(observedTotalBalance.sum(), durationInMs));
  }

  void runRound() {
    for (int i = 0; i < config.writesPerRound(); i++) {
      A from = randomAccount();
      A to = randomAccount();
      pool.execute(() -> {
        try {
          bank.transfer(from, to, 1);
        } catch (InsufficientFundsException e) {
          // ignore
        }
      });
    }

    for (int i = 0; i < config.readsPerRound(); i++) {
      A account = randomAccount();
      pool.execute(() -> {
        observedTotalBalance.add(account.balance());
      });
    }
  }
}
