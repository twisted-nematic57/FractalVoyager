/*** Main.java ****************************************************************\
 * Author:         twisted_nematic57                                          *
 * Date Created:   2025-12-26                                                 *
 * Description:    Initializes the Fractal Voyager system.                    *
 *                  - Initializes worker threads                              *
 *                  - Sets up the screen (emulates if physical not present)   *
\******************************************************************************/

import org.apfloat.Apcomplex;
import org.apfloat.ApcomplexMath;
import org.apfloat.Apfloat;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
  private static final boolean IS_PI = GraphicsEnvironment.isHeadless(); // If headless, likely running on a Pi
  private static final int THREADS = 4; // We're hardcoding 4 threads to be used due to the nature of the RPi Zero 2 W.
  private static final Object PRINT_LOCK = new Object(); // For safe multithreaded printing

  public static void main(String[] args) {
    System.out.println("Initializing Fractal Voyager.");
    System.out.println("Running on dedicated device?: " + IS_PI);
    System.out.println("Number of available cores: " + THREADS);
    System.out.println();

    // Load settings
    Properties config = new Properties();
    final String propertiesPath;

    if(IS_PI) {
      propertiesPath = "/home/tn57/FractalVoyager/FractalVoyager.properties";
    } else {
      propertiesPath = "FractalVoyager.properties";
    }

    try (InputStream in = new FileInputStream(propertiesPath)) {
      config.load(in); // Read the whole file, it shouldn't be large.
    } catch(IOException e) {
      System.out.println("ERROR: Couldn't load FractalVoyager.properties. (Does it exist?) Details:\n" + e.getMessage());
    }

    int iterations   = Integer.parseInt(config.getProperty("fv.iterations"));
    int maxPrecision = Integer.parseInt(config.getProperty("fv.maxPrecision"));

    ExecutorService pool = Executors.newFixedThreadPool(THREADS);

    for (int t = 0; t < THREADS; t++) {
      final int threadId = t;
      pool.submit(() -> ApfloatImpl(iterations, maxPrecision, threadId));
    }

    pool.shutdown();
  }

  public static void ApfloatImpl(int iterations, int precision, int threadId) {
    Apcomplex z = new Apcomplex(
        new Apfloat(0, precision),
        new Apfloat(0, precision)
    );

    Apcomplex c = new Apcomplex(
        new Apfloat(0.26, precision),
        new Apfloat(-0.14, precision)
    );

    for (int j = 0; j < 1000; j++) {
      long start = System.nanoTime();
      for (int i = 1; i <= iterations; i++) {
        z = ApcomplexMath.pow(z, 2).add(c);
      }
      long end = System.nanoTime();

      String msg = String.format(
          "[Thread %d] Took %.3f s to compute %d iterations with precision %d%n",
          threadId,
          (end - start) * 1e-9,
          iterations,
          precision);
      synchronized (PRINT_LOCK) {
        System.out.print(msg);
      }
    }
  }
}
