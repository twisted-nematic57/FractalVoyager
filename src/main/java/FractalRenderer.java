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
  public static Apfloat a, b, d, e, f, g, h, i, j, k; // Computational coefficients

  public static long iterate(Apcomplex c, int threadId) {
    long doneIterations = 0; // Since our first iteration is computed below, just by adding c.

    Apcomplex z = new Apcomplex(
        new Apfloat(0, maxPrecision),
        new Apfloat(0, maxPrecision)
    );

    for(int i = 0; i < maxIterations; i++) {
      //
      z = ApcomplexMath.pow(ApcomplexMath.pow(a.multiply(ApcomplexMath.pow(z, b)).divide(d), e).add(ApcomplexMath.pow(f.multiply(ApcomplexMath.pow(c, g)).divide(h), i)).divide(j), k);

      if(ApcomplexMath.norm(z).compareTo(escapeThreshold2) >= 0) { // If real^2 + imag^2 >= escape threshold; if it is then the point is outside the fractal
        break;
      }

      doneIterations++;
    }

    return doneIterations;
  }
}
