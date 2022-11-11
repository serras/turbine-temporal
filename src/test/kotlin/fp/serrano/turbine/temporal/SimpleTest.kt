package fp.serrano.turbine.temporal

import app.cash.turbine.test
import fp.serrano.turbine.temporal.formula.Formula
import fp.serrano.turbine.temporal.formula.always
import fp.serrano.turbine.temporal.formula.eventually
import fp.serrano.turbine.temporal.formula.holds
import io.kotest.assertions.shouldFail
import io.kotest.core.spec.style.AnnotationSpec
import kotlinx.coroutines.flow.flowOf

class SimpleTest: AnnotationSpec() {
  suspend fun simpleTest(block: () -> Formula<Int>) {
    flowOf(1, 2, 3).test {
      formula(block)
    }
  }

  @Test
  suspend fun `always holds`() {
    simpleTest {
      always { holds("> 0") { it > 0 } }
    }
  }

  @Test
  suspend fun `always doesn't hold`() {
    shouldFail {
      simpleTest {
        always { holds("< 2") { it < 2 } }
      }
    }
  }

  @Test
  suspend fun `eventually holds`() {
    simpleTest {
      eventually { holds("> 1") { it > 1 } }
    }
  }

  @Test
  suspend fun `eventually doesn't hold`() {
    shouldFail {
      simpleTest {
        eventually { holds("> 10") { it > 10 } }
      }
    }
  }
}