/*** PairCoefficient.java *****************************************************\
 * Author:         twisted_nematic57                                          *
 * Date Created:   2026-01-04                                                 *
 * Description:    Implements a coefficient class that can contain z itself,  *
 *                 a constant, or z multiplied by a constant.                 *
\******************************************************************************/

import org.apfloat.Apcomplex;


/*
   A "pair coefficient" is a coefficient that's plugged into an equation. It may contain z (a variable) and/or a
   constant. If, as specified by zPositions, z should not be plugged into the coefficient, then the second element is
   set to 1 to ensure it remains a user-specified constant. If z should be plugged in, then 
*/
public class PairCoefficient {
  // Multiplies each element in row 0 by its corresponding element in row 1 and return a 1D array of the results
  // Precondition: both rows must be the same length
  static Apcomplex[] multiplyRows(Apcomplex[][] params) {
    int length = params[0].length;
    Apcomplex[] result = new Apcomplex[length];
    for(int i = 0; i < length; i++) {
      result[i] = params[0][i].multiply(params[1][i]);
    }
    return result;
  }// Takes in a two-element array. Multiplies the first Apcomplex by the second Apcomplex.

  static Apcomplex multiplyCoefficient(Apcomplex[] c) {
    return c[0].multiply(c[1]);
  }
}
