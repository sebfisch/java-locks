package sebfisch.accounts;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.LongConsumer;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.LongStream;

public class Simulation<A extends Bank.Account> {

  static final int NUMBER_OF_THREADS = 16;
  static final long NUMBER_OF_TASKS = 10000000;
  static final int NUMBER_OF_EXECUTIONS = 5;
  static final int NUMBER_OF_ACCOUNTS = 10;
  static final int STARTING_BALANCE = 100000;

  record Scenario(long readShare, long writeShare) {
    long numberOfReads() {
      return readShare * NUMBER_OF_TASKS / (readShare + writeShare);
    }

    long numberOfWrites() {
      return writeShare * NUMBER_OF_TASKS / (readShare + writeShare);
    }
  }

  static final Scenario READ_ONLY = new Scenario(1, 0);
  static final Scenario WRITE_ONLY = new Scenario(0, 1);
  static final Scenario MORE_READS = new Scenario(100, 1);
  static final Scenario MORE_WRITES = new Scenario(1, 100);
  static final Scenario BOTH_EQUALLY = new Scenario(1, 1);

  public static void main(String[] args) throws InterruptedException {
    List<Supplier<Bank<?>>> bankSuppliers = List.of(
        IntrinsicBank::new,
        ReentrantBank::new,
        ReadWriteBank::new,
        StampedBank::new //
    );

    List<Scenario> configs = List.of(
        READ_ONLY,
        WRITE_ONLY,
        MORE_READS,
        MORE_WRITES,
        BOTH_EQUALLY //
    );

    for (var config : configs) {
      System.out.println(config);
      for (var newBank : bankSuppliers) {
        new Simulation<>(config, newBank.get()).run();
      }
    }
  }

  private final Scenario config;
  private final Bank<A> bank;

  private final List<A> accounts = new ArrayList<>();
  private final RandomGenerator rgen = RandomGeneratorFactory.getDefault().create();

  Simulation(Scenario config, Bank<A> bank) {
    System.out.println(bank.getClass().getSimpleName());

    this.config = config;
    this.bank = bank;

    for (int i = 0; i < NUMBER_OF_ACCOUNTS; i++) {
      A account = bank.createAccount();
      account.deposit(STARTING_BALANCE);
      accounts.add(account);
    }
  }

  A randomAccount() {
    return accounts.get(rgen.nextInt(accounts.size()));
  }

  void run() {
    RunTimes times = LongStream
        .generate(this::runOnce)
        .limit(NUMBER_OF_EXECUTIONS)
        .collect(RunTimes::new, (rts, t) -> rts.accept(t), RunTimes::combine);

    System.out.println("median: %dms, median absolute deviation: %dms"
        .formatted(times.median(), times.medianAbsoluteDeviation()));
  }

  long runOnce() {
    ExecutorService pool = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    LongAdder observedBalance = new LongAdder();
    LongAdder crashedTransfers = new LongAdder();

    Instant start = Instant.now();
    accessAccounts(pool, observedBalance, crashedTransfers);
    pool.shutdown();
    try {
      pool.awaitTermination(10, TimeUnit.SECONDS); // should finish sooner
    } catch (InterruptedException e) {
      System.out.println("interrupted after %dms"
          .formatted(Duration.between(start, Instant.now()).toMillis()));
    }
    long durationInMs = Duration.between(start, Instant.now()).toMillis();

    if (!pool.isTerminated()) {
      System.out.println("did not terminate after %dms".formatted(durationInMs));
    } else {
      System.out.println("observed balance is %d after %d crashed transfers and %dms"
          .formatted(observedBalance.sum(), crashedTransfers.sum(), durationInMs));
    }

    return durationInMs;
  }

  void accessAccounts(ExecutorService pool, LongAdder observedBalance, LongAdder crashedTransfers) {
    for (long i = 0; i < config.numberOfWrites(); i++) {
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
          crashedTransfers.increment();
        }
      });
    }

    for (long i = 0; i < config.numberOfReads(); i++) {
      final A account = randomAccount();
      pool.execute(() -> {
        observedBalance.add(account.balance());
      });
    }
  }

  record RunTimes(List<Long> times) implements LongConsumer {
    RunTimes() {
      this(new ArrayList<>());
    }

    @Override
    public void accept(long time) {
      times.add(time);
    }

    public RunTimes combine(RunTimes that) {
      List<Long> combinedTimes = new ArrayList<>(this.times());
      combinedTimes.addAll(that.times());
      return new RunTimes(combinedTimes);
    }

    public long median() {
      Collections.sort(times);
      return times.get(times.size() / 2);
    }

    public long medianAbsoluteDeviation() {
      long m = median();
      List<Long> deviations = new ArrayList<>();
      times.stream().map(time -> Math.abs(m - time)).forEach(deviations::add);
      return new RunTimes(deviations).median();
    }
  }
}
