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
  private final Apcomplex[][] params; // Function parameters: array because some functions in ApcomplexMath take multiple arguments
  private final boolean[] zPositions; // Variable indices in order: B, A, t, p, q, [math function args]
  private Apcomplex[] A, B, t, p, q;

  public Slot(String functionName, Apcomplex B, Apcomplex A, Apcomplex t, Apcomplex p, Apcomplex q, Apcomplex[] params, boolean[] zPositions) {
    this.function = FunctionRegistry.FractalFunctions.get(functionName);
    if(this.function == null) {
      throw new IllegalArgumentException("\nERROR: Unknown function \"" + functionName + "\"");
    }

    this.B = new Apcomplex[] {B, B}; // Yes, they're arrays. If z is not being plugged into them then it doesn't matter
    this.A = new Apcomplex[] {A, A}; // why; however if z is plugged in then it _does_ matter because z will be inserted
    this.t = new Apcomplex[] {t, t}; // into the second element before each evaluation and scaled by the first. This
    this.p = new Apcomplex[] {p, p}; // allows for more flexibility and fractal complexity.
    this.q = new Apcomplex[] {q, q};

    if(zPositions.length - 5 != params.length) { // -5 because first 5 elements are dedicated to B, A, t, p, q; all _after_ those are for more function parameters (if applicable)
      throw new IllegalArgumentException("\nERROR: z-positions for function parameters and function parameter lengths must match. z-positions: " + zPositions.length + "; params: " + params.length);
    }
    this.zPositions = zPositions;
    this.params = new Apcomplex[][] {params.clone(), params.clone()};
  }

  public Apcomplex eval(Apcomplex z) {
    // Insert z into the positions where z is supposed to be, or insert 1 if it's not supposed to be there
    if(zPositions[0]) B[1] = z; else B[1] = Apcomplex.ONE;
    if(zPositions[1]) A[1] = z; else A[1] = Apcomplex.ONE;
    if(zPositions[2]) t[1] = z; else t[1] = Apcomplex.ONE;
    if(zPositions[3]) p[1] = z; else p[1] = Apcomplex.ONE;
    if(zPositions[4]) q[1] = z; else q[1] = Apcomplex.ONE;
    for(int i = 0; i < params[0].length; i++) {
      if(zPositions[i+5]) {
        params[i][1] = z;
      } else {
        params[i][1] = Apcomplex.ONE;
      }
    }

    // B * func(A * t^p [,...] )^q
    return PairCoefficient.multiplyCoefficient(B) // B
        .multiply(ApcomplexMath.pow( // * ...^...
            function.apply( // func(
                PairCoefficient.multiplyCoefficient(A) // arg1
                    .multiply(ApcomplexMath.pow(PairCoefficient.multiplyCoefficient(t), PairCoefficient.multiplyCoefficient(p))), // * t^p
                PairCoefficient.multiplyRows(params)), // [arg2, arg3, ...] (if applicable)
            PairCoefficient.multiplyCoefficient(q))); // ...^q
  }
}
