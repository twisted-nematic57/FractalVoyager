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

  // Defines the one z-slot needed to get the Mandelbrot Set
  public static final Slot mandelbrotSet = new Slot("identity", new Apcomplex[0], new Apcomplex(new Apfloat(1), new Apfloat(0)), new Apcomplex(new Apfloat(2), new Apfloat(0)), new Apcomplex(new Apfloat(1), new Apfloat(0)), new Apcomplex(new Apfloat(1), new Apfloat(0)));

  // Basically just a dispatcher for actual iteration methods.
  public static long iterate(Slot s1, Slot s2, Slot s3, Apcomplex c, Apcomplex J, Apcomplex K, int threadId, Object PrintLock, boolean fast) {
    if(s1.equals(mandelbrotSet) && s2.equals(emptySlot) && s3.equals(emptySlot) && !fast) { // Are we literally just rendering the Mandelbrot Set in arbitrary precision?
      return iterate_mandelbrot(c, threadId, PrintLock); // Run the optimized arbitrary-precision method that handles only the Mandelbrot Set and runs much faster
    } else if(s1.equals(mandelbrotSet) && s2.equals(emptySlot) && s3.equals(emptySlot) && fast) { // If we're rendering the Mandelbrot Set using double precision (FASTEST)
      return iterate_mandelbrot_fast(c.real().doubleValue(), c.imag().doubleValue(), threadId, PrintLock);
    } else { // The fractal is not exactly the Mandelbrot Set, and we must do lots of extra work to account for it. Arbitrary precision always used for general fractals.
      return iterate_arbitrary_fractal(s1, s2, s3, c, J, K, threadId, PrintLock);
    }
  }

  // General fractal renderer: can handle multibrots, polynomial fractals, and many other custom cool fractals (extremely slow)
  public static long iterate_arbitrary_fractal(Slot s1, Slot s2, Slot s3, Apcomplex c, Apcomplex J, Apcomplex K, int threadId, Object PrintLock) {
    long doneIterations = 0;

    Apcomplex z = new Apcomplex(
        new Apfloat(0, maxPrecision),
        new Apfloat(0, maxPrecision)
    );

    for(int i = 0; i < maxIterations; i++) {
      long start = System.nanoTime();

      z = ApcomplexMath.pow(s1.eval(z).add(s2.eval(z)).add(s3.eval(z)).add(c).divide(J), K);

      // If real^2 + imag^2 >= (escape threshold)^2 then the point is outside the fractal
      if(z.real().multiply(z.real()).add(z.imag().multiply(z.imag())).compareTo(escapeThreshold2) >= 0) {
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
      synchronized(PrintLock) {
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

      // If real^2 + imag^2 >= (escape threshold)^2 then the point is outside the set
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

  // Fast Mandelbrot renderer: uses hardware-accelerated FP (doubles) to iterate. Extremely fast but also extremely limited.
  public static long iterate_mandelbrot_fast(double cr, double ci, int threadId, Object PrintLock) {
    long doneIterations = -1; // Used in an optimization to avoid incrementing every iteration
    final double escapeThreshold2_fast = escapeThreshold2.doubleValue();
    final double cr_fast = cr;
    final double ci_fast = ci;

    double zr = 0; // Real
    double zi = 0; // Imaginary

    for(int i = 0; i < maxIterations; i++) {
      // z = z^2 + c
      double temp = zr * zr - zi * zi + cr_fast;
      zi = 2.0 * zr * zi + ci_fast;
      zr = temp;

      // If real^2 + imag^2 >= (escape threshold)^2 then the point is outside the set
      if(zr * zr + zi * zi >= escapeThreshold2_fast) {
        doneIterations = i; // Optimization: no need to increment doneIterations every iteration
        /* Incrementing a basic type is so cheap compared to Apcomplex operations that I didn't bother to implement this
           optimization into previous, slower, more general/precise methods because it will make the code harder to
           understand and clutter it up.
        */

        break;
      }
    }
    if(doneIterations < 0) { // The escape threshold was never reached, so the point is stable within maxIterations.
      doneIterations = maxIterations;
    }

    return doneIterations;
  }
}
