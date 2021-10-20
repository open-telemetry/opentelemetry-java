/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.kotlin

import io.opentelemetry.api.trace.Span
import io.opentelemetry.context.Context
import io.opentelemetry.context.ContextKey
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension
import kotlinx.coroutines.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class KotlinCoroutinesTest {

  companion object {
    private val ANIMAL: ContextKey<String> = ContextKey.named("animal")

    @JvmField
    @RegisterExtension
    val otelTesting = OpenTelemetryExtension.create()
  }

  @Test
  fun runWithContext() {
    val context1 = Context.root().with(ANIMAL, "cat")
    assertThat(Context.current().get(ANIMAL)).isNull()
    runBlocking(Dispatchers.Default + context1.asContextElement()) {
      assertThat(Context.current().get(ANIMAL)).isEqualTo("cat")
      assertThat(coroutineContext.getOpenTelemetryContext()).isSameAs(Context.current())

      withContext(context1.with(ANIMAL, "dog").asContextElement()) {
        assertThat(Context.current().get(ANIMAL)).isEqualTo("dog")
      }

      assertThat(Context.current().get(ANIMAL)).isEqualTo("cat")

      async(Dispatchers.IO) {
        // Child coroutine inherits context automatically.
        assertThat(Context.current().get(ANIMAL)).isEqualTo("cat")
      }.await()

      coroutineScope {
        // Child coroutine inherits context automatically.
        assertThat(Context.current().get(ANIMAL)).isEqualTo("cat")
      }

      CoroutineScope(Dispatchers.IO).async {
        // Non-child coroutine does not inherit context automatically.
        assertThat(Context.current().get(ANIMAL)).isNull()
      }.await()
    }
  }

  @Test
  fun runWithSpan() {
    val span = otelTesting.openTelemetry.getTracer("test").spanBuilder("test")
      .startSpan()
    assertThat(Span.current()).isEqualTo(Span.getInvalid())
    runBlocking(Dispatchers.Default + span.asContextElement()) {
      assertThat(Span.current()).isEqualTo(span)
    }
  }

  @Test
  fun getOpenTelemetryContextOutsideOfContext() {
    runBlocking(Dispatchers.Default) {
      assertThat(Context.root()).isSameAs(coroutineContext.getOpenTelemetryContext())
    }
  }

  // Check whether concurrent coroutines leak context
  @Test
  @DelicateCoroutinesApi
  fun stressTest() {
    val context1 = Context.root().with(ANIMAL, "cat")
    runBlocking(context1.asContextElement()) {
      assertThat(Context.current().get(ANIMAL)).isEqualTo("cat")
      for (i in 0 until 100) {
        GlobalScope.launch {
          assertThat(Context.current().get(ANIMAL)).isEqualTo("cat")
          withContext(context1.with(ANIMAL, "dog").asContextElement()) {
            assertThat(Context.current().get(ANIMAL)).isEqualTo("dog")
            delay(10)
            assertThat(Context.current().get(ANIMAL)).isEqualTo("dog")
          }
        }
        GlobalScope.launch {
          assertThat(Context.current().get(ANIMAL)).isEqualTo("cat")
          withContext(context1.with(ANIMAL, "koala").asContextElement()) {
            assertThat(Context.current().get(ANIMAL)).isEqualTo("koala")
            delay(10)
            assertThat(Context.current().get(ANIMAL)).isEqualTo("koala")
          }
        }
      }
    }
  }
}
