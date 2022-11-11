# Module turbine-temporal

This package extends [Turbine](https://github.com/cashapp/turbine/) with temporal logic formulae. This means that you
can express how your flow behaves declaratively, using logical combinators. The "temporal" part refers to the fact that
the language to express the desired behavior has a notion of "sequence of messages" or "states".

For example, this is a test that specifies that for every element emitted in `inFlow` there should be a corresponding
message emitted in `outFlow` such that the result in the second is the argument plus 1.

```kotlin
@Test
suspend fun testCorrect() {
  merge(inFlow.map(::InMsg), outFlow.map(::OutMsg)).testFormula {
    inputOutput { i: InMsg, o: OutMsg ->
      o.value.arg == i.value && o.value.result == i.value + 1
    }
  }
}
```

In this case `inputOutput` is a high-level function which abstracts a common pattern. But you have the full power of
temporal logic at your fingertips! The formula above actually expands to:

```
always { input received => next { eventually { output received && condition holds } } }
```

That formula contains the three different ways in which we can talk about time:

- `always` specifies that the formula inside must be true on every point of the sequence,
- `next` specifies that the formula inside should be true exactly in the next state,
- `eventually` specified that the formula inside should be true in at least one following state.

In particular, `next + eventually` specifies that something must be true, beginning from the next element in the
sequence. In our case, we want that every time an input message is received, a corresponding output message is 
eventually received, that is, received in some later point in time.

The formula can be translated almost verbatim to Kotlin using the formula builders provided by the library.

```kotlin
@Test
suspend fun testCorrect() {
  merge(inFlow.map(::InMsg), outFlow.map(::OutMsg)).testFormula {
    always {
      implies(
        `if` = holds("input received") { it is InMsg },
        then = next { input: Msg ->
          eventually {
            holds("output is right") { output ->
              input is InMsg && output is OutMsg 
                      && o.value.arg == i.value && o.value.result == i.value + 1
            }
          }
        }
      )
    }
  }
}
```