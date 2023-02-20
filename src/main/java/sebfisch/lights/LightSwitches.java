package sebfisch.lights;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

// Implements "The Light Switch Problem"
// presented by Numberphile: https://www.youtube.com/watch?v=-UBDRX6bk-A
public class LightSwitches {

  // Lights can be switched on and off.
  static class Light {
    private boolean isSwitchedOn = false;

    synchronized boolean isSwitchedOn() {
      return isSwitchedOn;
    }

    synchronized void flipSwitch() {
      isSwitchedOn = !isSwitchedOn;
    }
  }

  public static void main(String[] args) throws InterruptedException {
    // There are many lights.
    final int count = 20000;
    final List<Light> lights = Stream.generate(Light::new).limit(count).toList();
    final ExecutorService pool = Executors.newCachedThreadPool();

    // There are as many people as there are lights.
    // They are both numbered starting from 1.
    for (int person = 1; person <= count; person++) {
      // Each person flips the switch on exactly those lights
      // corresponding to a multiple of their own number.
      for (int light = person; light <= count; light += person) {
        final int lightIndex = light - 1;
        // Unlike the original formulation, all people switch lights concurrently.
        pool.execute(() -> lights.get(lightIndex).flipSwitch());
      }
    }

    // Which lights are on after all people are done flipping switches?
    pool.shutdown();
    pool.awaitTermination(1, TimeUnit.MINUTES);

    // Print corresponding light numbers.
    System.out.println(IntStream
        .range(0, count)
        .filter(i -> lights.get(i).isSwitchedOn())
        .map(i -> i + 1)
        .mapToObj(Integer::toString)
        .collect(Collectors.joining(" ")));
  }

}
