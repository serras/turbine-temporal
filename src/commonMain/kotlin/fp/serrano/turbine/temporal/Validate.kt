package fp.serrano.turbine.temporal

import app.cash.turbine.Event
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import fp.serrano.turbine.temporal.formula.Formula
import fp.serrano.turbine.temporal.formula.StepResult
import fp.serrano.turbine.temporal.formula.done
import fp.serrano.turbine.temporal.formula.progress
import kotlinx.coroutines.flow.Flow
import kotlin.test.fail
import kotlin.time.Duration

/**
 * Checks that the formula holds over the [Flow].
 */
public suspend fun <A> Flow<A>.testFormula(
  timeout: Duration? = null,
  name: String? = null,
  block: () -> Formula<A>
) {
  this.test(timeout, name) { formula(block) }
}

/**
 * Checks that the formula holds. This version can be used inside Turbine's [test].
 */
public suspend fun <A> ReceiveTurbine<A>.formula(block: () -> Formula<A>) {
  formula(block())
}

/**
 * Checks that the formula holds. This version can be used inside Turbine's [test].
 */
public tailrec suspend fun <A> ReceiveTurbine<A>.formula(formula: Formula<A>) {
  when (val e = awaitEvent()) {
    is Event.Complete -> {
      formula.done().check(e)
    }
    is Event.Item -> {
      val step = formula.progress(Result.success(e.value))
      step.result.check(e)
      formula(step.next)
    }
    is Event.Error -> {
      val step = formula.progress(Result.failure(e.throwable))
      step.result.check(e)
      formula.done().check(e)
    }
  }
}

private fun <A> StepResult.check(e: Event<A>) {
  when (this) {
    null -> { } // ok
    else -> fail(
      "Formula falsified for $e:\n${joinToString(separator = "\n") { "- $it" }}"
    )
  }
}