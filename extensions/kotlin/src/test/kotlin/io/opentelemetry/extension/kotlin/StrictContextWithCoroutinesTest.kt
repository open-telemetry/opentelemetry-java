package io.opentelemetry.extension.kotlin

import io.opentelemetry.context.Context
import io.opentelemetry.context.ContextKey
import io.opentelemetry.context.ContextStorage
import io.opentelemetry.context.StrictContextStorage
import io.opentelemetry.sdk.testing.context.SettableContextStorageProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class StrictContextWithCoroutinesTest {

    companion object {
        private val ANIMAL: ContextKey<String> = ContextKey.named("animal")

        private lateinit var previousStorage: ContextStorage
        private lateinit var strictStorage: StrictContextStorage

        @BeforeAll
        @JvmStatic
        internal fun setUp() {
            previousStorage = SettableContextStorageProvider.getContextStorage()
            strictStorage = StrictContextStorage.create(previousStorage)
            SettableContextStorageProvider.setContextStorage(strictStorage)
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            SettableContextStorageProvider.setContextStorage(previousStorage)
        }
    }

    @Test
    fun noMakeCurrentSucceeds() {
        val context1 = Context.root().with(ANIMAL, "cat")
        assertThat(Context.current().get(ANIMAL)).isNull()
        runBlocking(Dispatchers.Default + context1.asContextElement()) {
            assertThat(Context.current().get(ANIMAL)).isEqualTo("cat")
        }
    }

    @Test
    fun noMakeCurrentNestedContextSucceeds() {
        val context1 = Context.root().with(ANIMAL, "cat")
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
        val context1 = Context.root().with(ANIMAL, "cat")
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
        val context1 = Context.root().with(ANIMAL, "cat")
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
        val context1 = Context.root().with(ANIMAL, "cat")
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
        val context1 = Context.root().with(ANIMAL, "cat")
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
        val context1 = Context.root().with(ANIMAL, "cat")
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
            // May be on a different thread, in which case ANIMAL != animal!
        }
    }

    suspend fun suspendingFunctionMakeCurrentWithManualClose(animal: String) {
        val scope = Context.current().with(ANIMAL, animal).makeCurrent()
        assertThat(Context.current().get(ANIMAL)).isEqualTo(animal)
        delay(10)
        // May be on a different thread, in which case ANIMAL != animal!
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
