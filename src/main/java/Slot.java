/*** Slot.java ****************************************************************\
 * Author:         twisted_nematic57                                          *
 * Date Created:   2026-01-01                                                 *
 * Description:    Defines behaviors for a z-slot.                            *
\******************************************************************************/

import org.apfloat.Apcomplex;
import org.apfloat.ApcomplexMath;


/* A z-slot is a (ideally non-constant) value that's added to the numerator of the general fractal
   equation as defined in Mockups/fractal_eqn.png:

   B * func(A * t^p)^q

   Each slot is designed to have either constants or z itself contained in each variable. z can be
   plugged into multiple variables, allowing z-slots like z^z.

   The Mandelbrot Set only uses one z-slot (z^2) and adds c. However, other fractals exist that
   rely on multiple terms involving z. Up to three z-slots are capable of yielding highly complex
   and visually interesting behavior without making it too chaotic to see patterns in. Each
   additional z-slot also adds a significant amount of computational complexity, so I refrained
   against adding support for more than 3 z-slots. Even two z-slots yield enough freedom to produce
   many, many interesting patterns.
*/

public class Slot {
  private final FractalFunction function;
  private final FractalFunctionExtendedParameters params; // Function parameters beyond t, which is always the first parameter
  private final boolean[] zPositions; // Variable indices in order: B, A, t, p, q, [math function args, if applicable]
  private PairCoefficient A, B, t, p, q;

  public Slot(String functionName, Apcomplex B, Apcomplex A, Apcomplex t, Apcomplex p, Apcomplex q, Apcomplex[] params, boolean[] zPositions) {
    this.function = FunctionRegistry.FractalFunctions.get(functionName);
    if(this.function == null) {
      throw new IllegalArgumentException("\nERROR: Unknown function \"" + functionName + "\"");
    }

    this.B = new PairCoefficient(B);
    this.A = new PairCoefficient(A);
    this.t = new PairCoefficient(t);
    this.p = new PairCoefficient(p);
    this.q = new PairCoefficient(q);

    if(zPositions.length - 5 != params.length) { // -5 because first 5 elements are dedicated to B, A, t, p, q; all _after_ those are for more function parameters (if applicable)
      throw new IllegalArgumentException("\nERROR: z-positions for function parameters and function parameter lengths must match. z-positions: " + zPositions.length + "; params: " + params.length);
    }
    this.zPositions = zPositions;
    this.params = new FractalFunctionExtendedParameters(params.clone());
  }

  public Apcomplex eval(Apcomplex z) {
    // Insert z into the positions where it's supposed to be
    B.insertz(z, zPositions[0]);
    A.insertz(z, zPositions[1]);
    t.insertz(z, zPositions[2]);
    p.insertz(z, zPositions[3]);
    q.insertz(z, zPositions[4]);
    for(int i = 0; i < params.length(); i++) {
      params.insertz(i, z, zPositions[i+5]); // i+5 because the first 5 elements of the zPositions array is for the
                                             // 5 hardcoded coefficients: B, A, t, p, q
    }

    // B * func(A * t^p [,...] )^q
    return B.computeScalar() // B
        .multiply(ApcomplexMath.pow( // * ...^...
            function.apply( // func(
                A.computeScalar() // arg1
                    .multiply(ApcomplexMath.pow(t.computeScalar(), p.computeScalar())), // * t^p
                params.computeScalarArray()), // [arg2, arg3, ...] (if applicable)
            q.computeScalar())); // ...^q
  }
}
