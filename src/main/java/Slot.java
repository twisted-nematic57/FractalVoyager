/*** Slot.java ****************************************************************\
 * Author:         twisted_nematic57                                          *
 * Date Created:   2026-01-01                                                 *
 * Description:    Defines behaviors for a z-slot.                            *
\******************************************************************************/

import org.apfloat.Apcomplex;
import org.apfloat.ApcomplexMath;

public class Slot {
  private final FractalFunction function;
  private final Apcomplex[] params;
  private final Apcomplex A, B;
  private final Apcomplex p, q;

  public Slot(String functionName, Apcomplex[] params, Apcomplex A, Apcomplex p, Apcomplex B, Apcomplex q) {
    this.function = FunctionRegistry.FUNCTIONS.get(functionName);
    if (this.function == null) {
      throw new IllegalArgumentException("ERROR: Unknown function \"" + functionName + "\"");
    }

    this.params = params;
    this.A = A;
    this.p = p;
    this.B = B;
    this.q = q;
  }

  public Apcomplex eval(Apcomplex z) {
    // B*func(A*z^p[,...])^q
    return B.multiply(ApcomplexMath.pow(function.apply(A.multiply(ApcomplexMath.pow(z, p)), params), q));
  }
}
