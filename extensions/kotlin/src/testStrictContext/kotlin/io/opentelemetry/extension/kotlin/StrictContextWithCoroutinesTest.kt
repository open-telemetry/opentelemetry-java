/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.kotlin

import io.opentelemetry.context.Context
import io.opentelemetry.context.ContextKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class StrictContextWithCoroutinesTest {

  companion object {
    private val ANIMAL: ContextKey<String> = ContextKey.named("animal")
  }

  @Test
  fun noMakeCurrentSucceeds() {
    val context1 = Context.groot().with(ANIMAL, "cat")
    assertThat(Context.current().get(ANIMAL)).isNull()
    runBlocking(Dispatchers.Default + context1.asContextElement()) {
      assertThat(Context.current().get(ANIMAL)).isEqualTo("cat")
    }
  }

  @Test
  fun noMakeCurrentNestedContextSucceeds() {
    val context1 = Context.groot().with(ANIMAL, "cat")
    assertThat(Context.current().get(ANIMAL)).isNull()
    runBlocking(Dispatchers.Default + context1.asContextElement()) {
      assertThat(Context.current().get(ANIMAL)).isEqualTo("cat")
      withContext(Context.current().with(ANIMAL, "dog").asContextElement()) {
        assertThat(Context.current().get(ANIMAL)).isEqualTo("dog")
      }
    }
  }

  @Test
  fun makeCurrentInNormalFunctionSucceeds() {
    assertThat(Context.current().get(ANIMAL)).isNull()
    nonSuspendingContextFunction("dog")
  }

  @Test
  fun makeCurrentInTopLevelCoroutineFails() {
    val context1 = Context.groot().with(ANIMAL, "cat")
    assertThat(Context.current().get(ANIMAL)).isNull()
    assertThatThrownBy {
      runBlocking(Dispatchers.Default + context1.asContextElement()) {
        assertThat(Context.current().get(ANIMAL)).isEqualTo("cat")
        Context.current().with(ANIMAL, "dog").makeCurrent().use {
          assertThat(Context.current().get(ANIMAL)).isEqualTo("dog")
        }
      }
    }.isInstanceOf(AssertionError::class.java)
  }

  @Test
  fun makeCurrentInNestedCoroutineFails() {
    val context1 = Context.groot().with(ANIMAL, "cat")
    assertThat(Context.current().get(ANIMAL)).isNull()
    assertThatThrownBy {
      runBlocking(Dispatchers.Default + context1.asContextElement()) {
        assertThat(Context.current().get(ANIMAL)).isEqualTo("cat")
        runBlocking(Dispatchers.Default) {
          assertThat(Context.current().get(ANIMAL)).isEqualTo("cat")
          Context.current().with(ANIMAL, "dog").makeCurrent().use {
            assertThat(Context.current().get(ANIMAL)).isEqualTo("dog")
          }
        }
      }
    }.isInstanceOf(AssertionError::class.java)
  }

  @Test
  fun makeCurrentInSuspendingFunctionFails() {
    val context1 = Context.groot().with(ANIMAL, "cat")
    assertThat(Context.current().get(ANIMAL)).isNull()
    assertThatThrownBy {
      runBlocking(Dispatchers.Default + context1.asContextElement()) {
        assertThat(Context.current().get(ANIMAL)).isEqualTo("cat")
        suspendingFunctionMakeCurrentWithAutoClose("dog")
      }
    }.isInstanceOf(AssertionError::class.java)
  }

  @Test
  fun makeCurrentInSuspendingFunctionWithManualCloseFails() {
    val context1 = Context.groot().with(ANIMAL, "cat")
    assertThat(Context.current().get(ANIMAL)).isNull()
    assertThatThrownBy {
      runBlocking(Dispatchers.Default + context1.asContextElement()) {
        assertThat(Context.current().get(ANIMAL)).isEqualTo("cat")
        suspendingFunctionMakeCurrentWithManualClose("dog")
      }
    }.isInstanceOf(AssertionError::class.java)
  }

  @Test
  fun noMakeCurrentInSuspendingFunctionSucceeds() {
    val context1 = Context.groot().with(ANIMAL, "cat")
    assertThat(Context.current().get(ANIMAL)).isNull()
    runBlocking(Dispatchers.Default + context1.asContextElement()) {
      assertThat(Context.current().get(ANIMAL)).isEqualTo("cat")
      suspendingFunctionContextElement("dog")
    }
  }

  // makeCurrent in non-suspending function is ok, the thread is guaranteed not to switch out from
  // under you.
  fun nonSuspendingContextFunction(animal: String) {
    Context.current().with(ANIMAL, animal).makeCurrent().use {
      assertThat(Context.current().get(ANIMAL)).isEqualTo(animal)
    }
  }
  suspend fun suspendingFunctionMakeCurrentWithAutoClose(animal: String) {
    Context.current().with(ANIMAL, animal).makeCurrent().use {
      assertThat(Context.current().get(ANIMAL)).isEqualTo(animal)
      delay(10)
      // The value of ANIMAL here is undefined - it may still be the original thread with
      // the ThreadLocal set correctly, or a completely different one with a different value.
      // So there's nothing we can assert here, and is precisely why we forbid makeCurrent in
      // suspending functions.
    }
  }

  suspend fun suspendingFunctionMakeCurrentWithManualClose(animal: String) {
    val scope = Context.current().with(ANIMAL, animal).makeCurrent()
    assertThat(Context.current().get(ANIMAL)).isEqualTo(animal)
    delay(10)
    // The value of ANIMAL here is undefined - it may still be the original thread with
    // the ThreadLocal set correctly, or a completely different one with a different value.
    // So there's nothing we can assert here, and is precisely why we forbid makeCurrent in
    // suspending functions.
    scope.close()
  }

  suspend fun suspendingFunctionContextElement(animal: String) {
    withContext(Context.current().with(ANIMAL, animal).asContextElement()) {
      assertThat(Context.current().get(ANIMAL)).isEqualTo(animal)
      delay(10)
      assertThat(Context.current().get(ANIMAL)).isEqualTo(animal)
    }
  }
}
