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
   set to 1 to ensure it remains a user-specified constant. If z should be plugged in, then it replaces the second
   element of the pair coefficient's array. This is because when the pair coefficient's value is used in computation,
   both elements are always multiplied to produce a scalar complex value. Again, this product scalar value is the one
   that's used in computation.

   This system ensures that a pair coefficient is always equal to either a constant, z, or z multiplied by a constant,
   and that it's never accidentally squared.
*/
public class PairCoefficient {
  public Apcomplex[] pair;

  public PairCoefficient(Apcomplex n) {
    this.pair = new Apcomplex[] {n, n}; // Both elements are initialized to the same value, but the second one will be
                                        // modified before external computations.
  }

  // Insert z into the position where z is supposed to be, or insert 1 if it's not supposed to be there
  public void insertz(Apcomplex z, boolean insert) {
    if(insert) {
      pair[1] = z;
    } else {
      pair[1] = Apcomplex.ONE;
    }
  }

  // Multiplies the first Apcomplex by the second Apcomplex.
  public Apcomplex computeScalar() {
    if(pair[1].equals(Apcomplex.ONE)) { // Optimization: don't call multiplication if we're just multiplying by 1
      return pair[0];
    } else {
      return pair[0].multiply(pair[1]);
    }
  }
}
