/*** FractalFunction.java *****************************************************\
 * Author:         twisted_nematic57                                          *
 * Date Created:   2026-01-01                                                 *
 * Description:    Defines an interface that enables application of an        *
 *                 arbitrary Apcomplex function on a variable number of       *
 *                 arguments.                                                 *
\******************************************************************************/

import org.apfloat.Apcomplex;

@FunctionalInterface
public interface FractalFunction {
  Apcomplex apply(Apcomplex x, Apcomplex[] extraParams);
}
