/*** FunctionRegistry.java ****************************************************\
 * Author:         twisted_nematic57                                          *
 * Date Created:   2026-01-01                                                 *
 * Description:    Defines methods for calling functions that the fractal     *
 *                 renderer supports.                                         *
\******************************************************************************/

import org.apfloat.ApcomplexMath;

import java.util.Map;

public class FunctionRegistry {
  private FunctionRegistry() {} // Prevent instantiation

  public static final Map<String, FractalFunction> FUNCTIONS = Map.of(
      "identity", (x, p) -> x,

      "sin", (x, p) ->
          ApcomplexMath.sin(x),

      "pow", (x, p) ->
          ApcomplexMath.pow(p[0], x),

      "gamma", (x, p) ->
          ApcomplexMath.gamma(x),

      "hyp2f1", (x, p) ->
          ApcomplexMath.hypergeometric2F1(
              p[0], p[1], p[2], x
          )
  );
}
