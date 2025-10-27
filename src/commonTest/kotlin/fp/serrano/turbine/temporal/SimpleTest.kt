package fp.serrano.turbine.temporal

import app.cash.turbine.test
import fp.serrano.turbine.temporal.formula.Formula
import fp.serrano.turbine.temporal.formula.always
import fp.serrano.turbine.temporal.formula.eventually
import fp.serrano.turbine.temporal.formula.holds
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFails

@Suppress("UNUSED")
class SimpleTest {
  suspend fun simpleTest(block: () -> Formula<Int>) {
    flowOf(1, 2, 3).test {
      formula(block)
    }
  }

  @Test
  fun `always holds`() = runTest {
    simpleTest {
      always { holds("> 0") { it > 0 } }
    }
  }

  @Test
  fun `always doesn't hold`() = runTest {
    assertFails {
      simpleTest {
        always { holds("< 2") { it < 2 } }
      }
    }
  }

  @Test
  fun `eventually holds`() = runTest {
    simpleTest {
      eventually { holds("> 1") { it > 1 } }
    }
  }

  @Test
  fun `eventually doesn't hold`() = runTest {
    assertFails {
      simpleTest {
        eventually { holds("> 10") { it > 10 } }
      }
    }
  }
}