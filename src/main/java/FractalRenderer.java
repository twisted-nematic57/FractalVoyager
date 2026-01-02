/*** FractalRenderer.java *****************************************************\
 * Author:         twisted_nematic57                                          *
 * Date Created:   2025-12-30                                                 *
 * Description:    Implements mathematical code that iterates on points of    *
 *                 fractals.                                                  *
\******************************************************************************/

import org.apfloat.Apcomplex;
import org.apfloat.ApcomplexMath;
import org.apfloat.Apfloat;

public class FractalRenderer {
  public static int maxIterations, maxPrecision;
  public static Apfloat escapeThreshold2; // Escape threshold is kept internally squared from what's stored in the config file for performance and convenience reasons.

  // Evaluates to zero
  public static final Slot emptySlot = new Slot("identity", new Apcomplex[0], new Apcomplex(new Apfloat(0), new Apfloat(0)), new Apcomplex(new Apfloat(1), new Apfloat(0)), new Apcomplex(new Apfloat(0), new Apfloat(0)), new Apcomplex(new Apfloat(1), new Apfloat(0)));

  // General fractal renderer: can handle multibrots, polynomial fractals, and many other custom cool fractals (extremely slow)
  public static long iterate(Slot s1, Slot s2, Slot s3, Apcomplex c, Apcomplex J, Apcomplex K, int threadId, Object PrintLock) {
    long doneIterations = 0;

    Apcomplex z = new Apcomplex(
        new Apfloat(0, maxPrecision),
        new Apfloat(0, maxPrecision)
    );

    for(int i = 0; i < maxIterations; i++) {
      long start = System.nanoTime();

      z = ApcomplexMath.pow(s1.eval(z).add(s2.eval(z)).add(s3.eval(z)).add(c).divide(J), K);

      if(ApcomplexMath.norm(z).compareTo(escapeThreshold2) >= 0) { // If real^2 + imag^2 >= escape threshold; if it is then the point is outside the fractal
        break;
      }

      doneIterations++;

      long end = System.nanoTime();

      String msg = String.format(
          "[Thread %d] Took %.3f s to compute iteration %d; config: %d iterations with precision %d%n",
          threadId,
          (end - start) * 1e-9,
          i,
          maxIterations,
          maxPrecision
      );
      synchronized (PrintLock) {
        System.out.print(msg);
      }
    }
    return doneIterations;
  }

  // Mandelbrot-specific renderer: uses arbitrary precision math to render the Mandelbrot Set in particular
  public static long iterate_mandelbrot(Apcomplex c, int threadId, Object PrintLock) {
    long doneIterations = 0;

    Apcomplex z = new Apcomplex(
        new Apfloat(0, maxPrecision),
        new Apfloat(0, maxPrecision)
    );

    long start = System.nanoTime();
    for(int i = 0; i < maxIterations; i++) {
      z = z.multiply(z).add(c);

      // If real^2 + imag^2 >= escape threshold, break; if it is then the point is outside the set
      if(z.real().multiply(z.real()).add(z.imag().multiply(z.imag())).compareTo(escapeThreshold2) >= 0) {
        break;
      }

      doneIterations++;
    }
    long end = System.nanoTime();
    String msg = String.format(
        "[Thread %d] Took %.3f s to compute %d iterations with precision %d\n",
        threadId,
        (end - start) * 1e-9,
        maxIterations,
        maxPrecision
    );
    synchronized (PrintLock) {
      System.out.print(msg);
    }

    return doneIterations;
  }

  // Fast Mandelbrot renderer: uses doubles to iterate. Extremely fast but also extremely limited.
  public static long iterate_mandelbrot_fast(double cr, double ci, int threadId, Object PrintLock) {
    long doneIterations = 0;
    final double escapeThreshold2_fast = escapeThreshold2.doubleValue();
    final double cr_fast = cr;
    final double ci_fast = ci;

    double zr = 0; // Real
    double zi = 0; // Imaginary

    for(int j = 0; j < 1000000; j++) {
      long start = System.nanoTime();
      for(int i = 0; i < maxIterations; i++) {
        // z = z^2 + c
        double temp = zr * zr - zi * zi + cr_fast;
        zi = 2.0 * zr * zi + ci_fast;
        zr = temp;
        if(zr * zr + zi * zi >= escapeThreshold2_fast) {
          doneIterations = i;
          break;
        }
      }
      long end = System.nanoTime();

      String msg = String.format(
          "[Thread %d] Took %.3f μs to compute %d iterations with precision %d\n",
          threadId,
          (end - start) * 1e-3,
          maxIterations,
          maxPrecision
      );
      synchronized (PrintLock) {
        System.out.print(msg);
      }
    }

    return doneIterations;
  }
}
