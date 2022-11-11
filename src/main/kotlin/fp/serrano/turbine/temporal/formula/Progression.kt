package fp.serrano.turbine.temporal.formula

// Based on the Quickstrom paper
// https://arxiv.org/pdf/2203.11532.pdf,
// which is in turned based on formula progression
// https://users.cecs.anu.edu.au/~thiebaux/papers/icaps05.pdf

internal typealias Problem = String

private val everythingOk: List<Problem>? = null
private fun problem(message: String): List<Problem> = listOf(message)
private fun List<Problem>?.isOk() = this == null

internal typealias StepResult = List<Problem>?
private fun List<StepResult>.andResults() =
  if (all { it.isOk() })
    everythingOk
  else
    flatMap { it.orEmpty() }
private fun List<StepResult>.orResults() =
  if (any { it.isOk() })
    everythingOk
  else
    flatMap { it.orEmpty() }

public data class Step<A>(val result: StepResult, val next: Formula<A>)

public fun <A> Atomic<A>.atomicProgress(x: Result<A>): StepResult = when (this) {
  is TRUE -> everythingOk
  is FALSE -> problem("fail")
  is Predicate -> x.fold(
    onSuccess = { if (this.test(it)) everythingOk else problem(message) },
    onFailure = { problem("Unexpected exception, expecting value") }
  )
  is Throws -> x.fold(
    onSuccess = { problem("Unexpected item, expecting exception") },
    onFailure = { if (this.test(it)) everythingOk else problem(message) }
  )
}

public fun <A> Formula<A>.progress(x: Result<A>): Step<A> = when (this) {
  is Atomic ->
    Step(this.atomicProgress(x), TRUE)
  is Not -> {
    if (formula.atomicProgress(x).isOk()) {
      Step(problem("Negated condition was true"), TRUE)
    } else {
      Step(everythingOk, TRUE)
    }
  }
  is And -> {
    val steps = formulae.map { it.progress(x) }
    val result = steps.map { it.result }.andResults()
    Step(result, and(steps.map { it.next }))
  }
  is Or -> {
    val steps = formulae.map { it.progress(x) }
    val result = steps.map { it.result }.orResults()
    Step(result, or(steps.map { it.next }))
  }
  is Implies -> {
    val leftResult = `if`.atomicProgress(x)
    if (leftResult.isOk()) {
      // if left is true, we check the right
      then.progress(x)
    } else {
      // otherwise the formula is true (false => x == true)
      Step(everythingOk, TRUE)
    }
  }
  is Next -> Step(everythingOk, formula)
  is DependentNext -> {
    x.fold(
      // we use the current value to compute the next step
      onSuccess = { Step(everythingOk, formula(it)) },
      // there are no more steps after an exception
      onFailure = { Step(everythingOk, TRUE) }
    )
  }
  is Always -> {
    // when we have always it has to be true
    // 1. in this state,
    // 2. in any other next state
    val step = formula.progress(x)
    Step(step.result, and(step.next, this))
  }
  is Eventually -> {
    val step = formula.progress(x)
    if (step.result.isOk()) {
      // this one is true, so we're done
      Step(everythingOk, TRUE)
    } else {
      // we have to try in the next one
      // so if we are done we haven't proved it yet
      Step(everythingOk,this)
    }
  }
}

// is there something missing to prove?
// if we have 'eventually', we cannot conclude
public fun <A> Formula<A>.done(): StepResult = when (this) {
  // atomic predicates are done
  is Atomic -> everythingOk
  is Not -> everythingOk
  // if we have 'and' and 'or', combine
  is And -> formulae.map { it.done() }.andResults()
  is Or -> formulae.map { it.done() }.orResults()
  is Implies -> listOf(`if`.done(), then.done()).andResults()
  // we have nothing missing here
  is Next -> everythingOk
  is DependentNext -> everythingOk
  is Always -> everythingOk
  // we have an 'eventually' missing
  is Eventually -> problem("Should hold eventually: ${formula.pretty()}")
}