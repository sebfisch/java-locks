package sebfisch.accounts;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public class Simulation<A extends Bank.Account> {

  static final int STARTING_BALANCE = 100;

  record Config(
      int numberOfAccounts,
      int numberOfReads,
      int numberOfWrites) {
  }

  static final Config READ_ONLY = new Config(
      10,
      20000,
      0);

  static final Config WRITE_ONLY = new Config(
      10,
      0,
      20000);

  static final Config MORE_READS = new Config(
      10,
      20000,
      200);

  static final Config MORE_WRITES = new Config(
      10,
      200,
      20000);

  public static void main(String[] args) throws InterruptedException {
    List<Supplier<Bank<?>>> bankSuppliers = List.of(
        IntrinsicBank::new,
        ReentrantBank::new,
        ReadWriteBank::new,
        StampedBank::new //
    );

    List<Config> configs = List.of(
        READ_ONLY,
        WRITE_ONLY,
        MORE_READS,
        MORE_WRITES //
    );

    for (var newBank : bankSuppliers) {
      for (var config : configs) {
        new Simulation<>(config, newBank.get()).run();
      }
    }
  }

  private final Config config;
  private final Bank<A> bank;

  private final List<A> accounts = new ArrayList<>();
  private final RandomGenerator rgen = RandomGeneratorFactory.getDefault().create();

  Simulation(Config config, Bank<A> bank) {
    this.config = config;
    this.bank = bank;

    for (int i = 0; i < config.numberOfAccounts(); i++) {
      A account = bank.createAccount();
      account.deposit(STARTING_BALANCE);
      accounts.add(account);
    }
  }

  A randomAccount() {
    return accounts.get(rgen.nextInt(accounts.size()));
  }

  void run() throws InterruptedException {
    System.out.println("%s %s".formatted(bank.getClass().getSimpleName(), config));
    runOnce();
    runOnce();
    runOnce();
  }

  void runOnce() throws InterruptedException {
    ExecutorService pool = Executors.newCachedThreadPool();
    LongAdder observedBalance = new LongAdder();

    Instant start = Instant.now();
    accessAccounts(pool, observedBalance);
    pool.shutdown();
    pool.awaitTermination(10, TimeUnit.SECONDS); // should finish sooner
    long durationInMs = Duration.between(start, Instant.now()).toMillis();

    if (!pool.isTerminated()) {
      System.out.println("did not terminate after %dms".formatted(durationInMs));
    } else {
      System.out.println("observed balance is %d after %dms"
          .formatted(observedBalance.sum(), durationInMs));
    }
  }

  void accessAccounts(ExecutorService pool, LongAdder observedBalance) {
    for (int i = 0; i < config.numberOfWrites(); i++) {
      final A from = randomAccount();
      A other = randomAccount();
      while (from == other) {
        other = randomAccount();
      }
      final A to = other;
      pool.execute(() -> {
        try {
          bank.transfer(from, to, 1);
        } catch (InsufficientFundsException e) {
          // ignore
        }
      });
    }

    for (int i = 0; i < config.numberOfReads(); i++) {
      final A account = randomAccount();
      pool.execute(() -> {
        observedBalance.add(account.balance());
      });
    }
  }
}
