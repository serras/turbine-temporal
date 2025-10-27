package fp.serrano.turbine.temporal

import fp.serrano.turbine.temporal.patterns.inputOutput
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFails

sealed interface Msg
data class InMsg(val value: Int): Msg
data class OutMsg(val value: Pair<Int, Int>): Msg

class InAndOutTest {
  val inFlow = flowOf(1, 2, 3, 4)

  val correctOutFlow = inFlow.map {
    delay(500)  // this represents work
    it to it + 1
  }

  val wrongOutFlow = inFlow.map {
    delay(500)  // this represents work
    it to (if (it == 2) 5 else it + 1)
  }

  @Test
  fun correct() = runTest {
    merge(inFlow.map(::InMsg), correctOutFlow.map(::OutMsg)).testFormula {
      inputOutput { i: InMsg, o: OutMsg ->
        o.value.first == i.value && o.value.second == i.value + 1
      }
    }
  }

  @Test
  fun wrong() = runTest {
    assertFails {
      merge(inFlow.map(::InMsg), wrongOutFlow.map(::OutMsg)).testFormula {
        inputOutput { i: InMsg, o: OutMsg ->
          o.value.first == i.value && o.value.second == i.value + 1
        }
      }
    }
  }
}