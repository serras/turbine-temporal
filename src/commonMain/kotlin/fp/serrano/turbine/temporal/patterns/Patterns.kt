package fp.serrano.turbine.temporal.patterns

import fp.serrano.turbine.temporal.formula.Formula
import fp.serrano.turbine.temporal.formula.always
import fp.serrano.turbine.temporal.formula.eventually
import fp.serrano.turbine.temporal.formula.holds
import fp.serrano.turbine.temporal.formula.implies
import fp.serrano.turbine.temporal.formula.next

/**
 * General pattern for input-output behavior.
 * The obtained formula specifies that for every input message
 * of type [I] there should be a corresponding output message
 * of type [O] which satisfies the relation given by [predicate].
 *
 * In general you want to use this function over the `merge` of
 * the input and output flows, where you somehow mark the provenance
 * of each message in some way.
 *
 * ```kotlin
 * merge(
 *   inputFlow.map { In(it) },
 *   outputFlow.map { Out(it) }
 * ).testFormula {
 *   inputOutput { input: In, output: Out ->
 *     // do something
 *   }
 * }
 * ```
 */
public inline fun <M, reified I: M, reified O: M> inputOutput(
  inputMessage: String = "input message",
  outputMessage: String = "output message",
  crossinline predicate: (I, O) -> Boolean
): Formula<M> =
  // always { input message => next { eventually { output message } } }
  always {
    implies(
      `if` = holds(inputMessage) { it is I },
      then = next { input: M ->
        eventually {
          holds(outputMessage) { output ->
            input is I && output is O && predicate(input, output)
          }
        }
      }
    )
  }