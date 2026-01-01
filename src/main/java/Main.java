/*** Main.java ****************************************************************\
 * Author:         twisted_nematic57                                          *
 * Date Created:   2025-12-26                                                 *
 * Description:    Initializes the Fractal Voyager system.                    *
 *                  - Initializes worker threads                              *
 *                  - Sets up the screen (emulates if physical not present)   *
\******************************************************************************/

import org.apfloat.Apcomplex;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {
  private static final boolean IS_PI = GraphicsEnvironment.isHeadless(); // If headless, likely running on a Pi
  private static final int THREADS = 4; // We're hardcoding 4 threads to be used due to the nature of the RPi Zero 2 W.

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

    FractalRenderer.maxIterations = Integer.parseInt(config.getProperty("fv.defaults.iterations"));
    FractalRenderer.maxPrecision = Integer.parseInt(config.getProperty("fv.defaults.maxPrecision"));
    // Escape threshold is squared from what's stored in the config file for performance and convenience reasons.
    FractalRenderer.escapeThreshold = ApfloatMath.pow(new Apfloat(config.getProperty("fv.defaults.escapeThreshold")), 2);

    Apcomplex c = new Apcomplex( // Escapes at 8 iterations
        new Apfloat("-0.8130614", FractalRenderer.maxPrecision),
        new Apfloat("0.3311725", FractalRenderer.maxPrecision)
    );

    System.out.println(FractalRenderer.iterate(c, 0));
  }
}
