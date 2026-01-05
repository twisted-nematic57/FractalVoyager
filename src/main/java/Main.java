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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {
  private static final boolean IS_PI = GraphicsEnvironment.isHeadless(); // If headless, likely running on a Pi
  private static final int THREADS = Runtime.getRuntime().availableProcessors();

  public static void main(String[] args) {
    System.out.println("Initializing Fractal Voyager.");
    System.out.println("Running on dedicated device?: " + IS_PI);
    System.out.println("Number of available threads: " + THREADS);
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

    FractalIterator.maxIterations = Integer.parseInt(config.getProperty("fv.defaults.iterations"));
    FractalIterator.maxPrecision = Integer.parseInt(config.getProperty("fv.defaults.maxPrecision"));
    // Escape threshold is squared from what's stored in the config file for performance and convenience reasons.
    FractalIterator.escapeThreshold2 = ApfloatMath.pow(new Apfloat(config.getProperty("fv.defaults.escapeThreshold")), 2);

    /*Slot s1 = FractalIterator.mandelbrotSet;
    Slot s2 = FractalIterator.emptySlot;
    Slot s3 = FractalIterator.emptySlot;
    Apcomplex J = Apcomplex.ONE;
    Apcomplex K = Apcomplex.ONE;*/

    // ------------------------------------------------------------------
    // Render the Mandelbrot set (fast, double precision) into a PNG file
    // ------------------------------------------------------------------

    // ---- 1.  Set up the iterator --------------------------------------------------
        final boolean fast = true;                                   // use FP
        final Slot s1 = FractalIterator.mandelbrotSet;               // only one slot needed
        final Slot s2 = FractalIterator.emptySlot;
        final Slot s3 = FractalIterator.emptySlot;
        final Apcomplex J = Apcomplex.ONE;                           // not used for Mandelbrot
        final Apcomplex K = Apcomplex.ONE;
        final boolean[] zPositions = new boolean[] {false, false};                 // dummy – not used

        final FractalIterator mandelbrotIter =
            new FractalIterator(s1, s2, s3, J, K, zPositions, fast);

    // ---- 2.  Create the image ---------------------------------------------------
        final int width  = 1024;
        final int height = 768;
        final BufferedImage image = new BufferedImage(width, height,
            BufferedImage.TYPE_USHORT_GRAY);
        final WritableRaster raster = image.getRaster();

    // ---- 3.  Define the complex‑plane limits ------------------------------------
        final double xmin = -2.333;
        final double xmax =  1.0;
        final double ymin = -1.25;
        final double ymax =  1.25;

    // ---- 4.  Render -------------------------------------------------------------
        for (int y = 0; y < height; y++) {
          // Map pixel row to imaginary coordinate (top → ymin, bottom → ymax)
          final double ci = ymax - (y / (double)(height - 1)) * (ymax - ymin);

          for (int x = 0; x < width; x++) {
            // Map pixel column to real coordinate
            final double cr = xmin + (x / (double)(width - 1)) * (xmax - xmin);

            // Fast Mandelbrot iteration (double precision)
            final long iter = mandelbrotIter.iterate_mandelbrot_fast(cr, ci);

            // ---- 5.  Map iteration count to 16‑bit grayscale ----------------
            //   * 1 iteration → white (65535)
            //   * maxIterations → black (0)
            int gray;
            if (iter >= FractalIterator.maxIterations) {
              gray = 0;                                     // fully black
            } else {
              // Linear interpolation: 1 -> 65535, max -> 0
              double frac = (double)(FractalIterator.maxIterations - iter) /
                  (double)(FractalIterator.maxIterations - 1);
              gray = (int)Math.round(frac * 65535.0);
              if (gray > 65535) gray = 65535;
              if (gray < 0)      gray = 0;
            }

            raster.setSample(x, y, 0, gray);
          }
        }

    // ---- 6.  Save the PNG -------------------------------------------------------
        try {
          final File output = new File("mandelbrot.png");
          ImageIO.write(image, "png", output);
          System.out.println("Mandelbrot image written to " + output.getAbsolutePath());
        } catch (IOException e) {
          System.err.println("ERROR: Could not write PNG file: " + e.getMessage());
        }
      }
}
