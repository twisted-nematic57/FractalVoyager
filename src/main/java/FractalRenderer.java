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
  public Slot s1, s2, s3;
  public PairCoefficient J, K;
  public boolean[] zPositions; // Only 2 elements long in this class.
  public boolean fast; // If computing the Mandelbrot Set, should we use FP (doubles)? true = yes, false = use slow arbitrary precision

  // Evaluates to zero
  public static final Slot emptySlot = new Slot(
      "identity",
      Apcomplex.ZERO,
      Apcomplex.ONE,
      Apcomplex.ONE,
      Apcomplex.ONE,
      Apcomplex.ONE,
      new Apcomplex[0],
      new boolean[] {false, false, false, false, false}
  );

  // Defines the one z-slot needed to get the Mandelbrot Set
  public static final Slot mandelbrotSet = new Slot(
      "identity",
      Apcomplex.ONE,
      Apcomplex.ONE,
      Apcomplex.ONE,
      new Apcomplex(new Apfloat(2), new Apfloat(0)),
      Apcomplex.ONE,
      new Apcomplex[0],
      new boolean[] {false, false, true, false, false}
  );

  public FractalRenderer(Slot s1, Slot s2, Slot s3, Apcomplex J, Apcomplex K, boolean[] zPositions, boolean fast) {
    this.s1 = s1;
    this.s2 = s2;
    this.s3 = s3;
    this.J = new PairCoefficient(J);
    this.K = new PairCoefficient(K);
    this.zPositions = zPositions;
    this.fast = fast;
  }

  // Basically just a dispatcher for actual iteration methods.
  // Slots s1, s2 and s3 must be populated with something, they can't be null; set them to emptySlot if you want them to evaluate to zero.
  // If rendering just the Mandelbrot Set, set s1 to mandelbrotSet and call it a day.
  // For lowest runtime, use the slots in order, i.e. prefer using the lowest-numbered slots before using the higher-numbered ones.
  public long iterate(Apcomplex c) {
    if(s1.equals(mandelbrotSet) && s2.equals(emptySlot) && s3.equals(emptySlot) && fast) { // Mandelbrot Set with double precision
      return iterate_mandelbrot_fast(c.real().doubleValue(), c.imag().doubleValue());
    } else if(s1.equals(mandelbrotSet) && s2.equals(emptySlot) && s3.equals(emptySlot) && !fast) { // Mandelbrot Set with arbitrary precision
      return iterate_mandelbrot(c);
    } else if(!s1.equals(emptySlot) && s2.equals(emptySlot) && s3.equals(emptySlot)) { // 1-slot fractal with arbitrary precision
      return iterate_arbitrary_fractal_1s(c);
    } else if(!s1.equals(emptySlot) && !s2.equals(emptySlot) && s3.equals(emptySlot)) { // 2-slot fractal with arbitrary precision
      return iterate_arbitrary_fractal_2s(c);
    } else { // 3-slot fractal with arbitrary precision
      return iterate_arbitrary_fractal_3s(c);
    }
  }

  // General 3-slot fractal iterator: can handle multibrots, polynomial fractals, and many other custom cool fractals (extremely slow)
  public long iterate_arbitrary_fractal_3s(Apcomplex c) {
    long doneIterations = 0;

    Apcomplex z = new Apcomplex(
        new Apfloat(0, maxPrecision),
        new Apfloat(0, maxPrecision)
    );

    for(int i = 0; i < maxIterations; i++) {
      // If z is supposed to be plugged in to J or K, then apply that
      J.insertz(z, zPositions[0]);
      K.insertz(z, zPositions[1]);

      z = ApcomplexMath.pow(s1.eval(z).add(s2.eval(z)).add(s3.eval(z)).add(c).divide(J.computeScalar()), K.computeScalar());

      // If real^2 + imag^2 >= (escape threshold)^2 then the point is outside the fractal
      if(z.real().multiply(z.real()).add(z.imag().multiply(z.imag())).compareTo(escapeThreshold2) >= 0) {
        break;
      }

      doneIterations++;
    }
    return doneIterations;
  }

  // General 2-slot fractal iterator: extremely slow general iterator, but still significantly faster than the 3-slot version
  public long iterate_arbitrary_fractal_2s(Apcomplex c) {
    long doneIterations = 0;

    Apcomplex z = new Apcomplex(
        new Apfloat(0, maxPrecision),
        new Apfloat(0, maxPrecision)
    );

    for(int i = 0; i < maxIterations; i++) {
      // If z is supposed to be plugged in to J or K, then apply that
      J.insertz(z, zPositions[0]);
      K.insertz(z, zPositions[1]);

      z = ApcomplexMath.pow(s1.eval(z).add(s2.eval(z)).add(c).divide(J.computeScalar()), K.computeScalar());

      // If real^2 + imag^2 >= (escape threshold)^2 then the point is outside the fractal
      if(z.real().multiply(z.real()).add(z.imag().multiply(z.imag())).compareTo(escapeThreshold2) >= 0) {
        break;
      }

      doneIterations++;
    }
    return doneIterations;
  }

  // General 1-slot fractal iterator: fastest generic 1-slot iterator
  public long iterate_arbitrary_fractal_1s(Apcomplex c) {
    long doneIterations = 0;

    Apcomplex z = new Apcomplex(
        new Apfloat(0, maxPrecision),
        new Apfloat(0, maxPrecision)
    );

    for(int i = 0; i < maxIterations; i++) {
      // If z is supposed to be plugged in to J or K, then apply that
      J.insertz(z, zPositions[0]);
      K.insertz(z, zPositions[1]);

      z = ApcomplexMath.pow(s1.eval(z).add(c).divide(J.computeScalar()), K.computeScalar());

      // If real^2 + imag^2 >= (escape threshold)^2 then the point is outside the fractal
      if(z.real().multiply(z.real()).add(z.imag().multiply(z.imag())).compareTo(escapeThreshold2) >= 0) {
        break;
      }

      doneIterations++;
    }
    return doneIterations;
  }

  // Mandelbrot-specific renderer: uses arbitrary precision math to render the Mandelbrot Set in particular
  public long iterate_mandelbrot(Apcomplex c) {
    long doneIterations = 0;

    Apcomplex z = new Apcomplex(
        new Apfloat(0, maxPrecision),
        new Apfloat(0, maxPrecision)
    );

    for(int i = 0; i < maxIterations; i++) {
      // z = z^2 + c
      z = z.multiply(z).add(c);

      // If real^2 + imag^2 >= (escape threshold)^2 then the point is outside the set
      if(z.real().multiply(z.real()).add(z.imag().multiply(z.imag())).compareTo(escapeThreshold2) >= 0) {
        break;
      }

      doneIterations++;
    }

    return doneIterations;
  }

  // Fast Mandelbrot renderer: uses hardware-accelerated FP (doubles) to iterate. Extremely fast but also extremely limited.
  public long iterate_mandelbrot_fast(double cr, double ci) {
    long doneIterations = -1; // Used in an optimization to avoid incrementing every iteration

    final double escapeThreshold2_fast = escapeThreshold2.doubleValue();
    final double cr_fast = cr; // Real part of c
    final double ci_fast = ci; // Imaginary part of c
    double zr = 0; // Real part of z
    double zi = 0; // Imaginary part of z

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
