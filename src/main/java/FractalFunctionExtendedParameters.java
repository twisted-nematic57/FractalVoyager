/*** FractalFunctionExtendedParameters.java ***********************************\
 * Author:         twisted_nematic57                                          *
 * Date Created:   2026-01-04                                                 *
 * Description:    Implements a wrapper for an array of PairCoefficients and  *
 *                 conveniently handles plugging in constants, z, or z        *
 *                 multiplied by a constant into the various supported        *
 *                 fractal functions.                                         *
\******************************************************************************/

import org.apfloat.Apcomplex;

/* Fractal function extended parameters are the parameters passed to multi-input functions that can be used to generate
   fractals. They're called "extended" because they only contain arguments after the first one, for convenience reasons.
   Since it's possible to plug in constants, z itself, or z multiplied by constants into these arguments, they're stored
   in a similar fashion to PairCoefficients. Since there can be more than two arguments to one function, the arguments
   are stored as an array of PairCoefficients.

   To understand the code below I recommend reading the PairCoefficient code first, as much of it is just an extension
   of that and lesser-commented.
*/
public class FractalFunctionExtendedParameters {
  public PairCoefficient[] params;

  public FractalFunctionExtendedParameters(Apcomplex[] params) {
    this.params = new PairCoefficient[params.length];
    for(int i = 0; i < params.length; i++) {
      this.params[i] = new PairCoefficient(params[i]);
    }
  }

  // Insert z into the coefficients where z is supposed to be
  public void insertz(int index, Apcomplex z, boolean insert) {
    params[index].insertz(z, insert);
  }

  // Computes the scalar value of each PairCoefficient and returns a 1D array of the results
  // Precondition: both columns must be the same length
  public Apcomplex[] computeScalarArray() {
    int length = params.length;

    Apcomplex[] result = new Apcomplex[length];
    for(int i = 0; i < length; i++) {
      result[i] = params[i].computeScalar();
    }

    return result;
  }

  public int length() {
    return params.length;
  }
}
