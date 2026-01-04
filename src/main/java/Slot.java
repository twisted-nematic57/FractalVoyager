/*** Slot.java ****************************************************************\
 * Author:         twisted_nematic57                                          *
 * Date Created:   2026-01-01                                                 *
 * Description:    Defines behaviors for a z-slot.                            *
\******************************************************************************/

import org.apfloat.Apcomplex;
import org.apfloat.ApcomplexMath;


/* A z-slot is a non-constant value that's added to the numerator of the general fractal equation as defined in
   Mockups/fractal_eqn.png:

   B * func(A * z^p)^q

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
  private final Apcomplex[] params; // Function parameters: array because some functions in ApcomplexMath take multiple arguments
  private final boolean[] zPositions; // Variable indices in order: B, A, p, q, [math function args]; for each index, if true, z will be used there instead of a constant.
  private Apcomplex A, B;
  private Apcomplex p, q;

  public Slot(String functionName, Apcomplex B, Apcomplex A, Apcomplex p, Apcomplex q, Apcomplex[] params, boolean[] zPositions) {
    this.function = FunctionRegistry.FractalFunctions.get(functionName);
    if (this.function == null) {
      throw new IllegalArgumentException("\nERROR: Unknown function \"" + functionName + "\"");
    }

    this.B = B;
    this.A = A;
    this.p = p;
    this.q = q;
    this.params = params;
    this.zPositions = zPositions;
  }

  public Apcomplex eval(Apcomplex z) {
    // Insert z into the positions where z is supposed to be
    if(zPositions[0]) B = z;
    if(zPositions[1]) A = z;
    if(zPositions[2]) p = z;
    if(zPositions[3]) q = z;
    for(int i = 0; i < params.length; i++) {
      if(zPositions[i+4]) params[i] = z;
    }

    // B * func(A * z^p [,...] )^q
    return B.multiply(ApcomplexMath.pow(function.apply(A.multiply(ApcomplexMath.pow(z, p)), params), q));
  }
}
