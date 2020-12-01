package io.opentelemetry.extension.kotlin

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.context.Context
import io.opentelemetry.context.ContextKey
import kotlinx.coroutines.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KotlinCoroutinesTest {

    companion object {
        private val ANIMAL: ContextKey<String> = ContextKey.named("animal")
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

            GlobalScope.launch {
                // Child coroutine inherits context automatically.
                assertThat(Context.current().get(ANIMAL)).isEqualTo("cat")
            }
        }
    }

    @Test
    fun runWithSpan() {
        val span = OpenTelemetry.getGlobalTracer("test").spanBuilder("test").startSpan()
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
