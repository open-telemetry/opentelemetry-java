package io.opentelemetry.extension.kotlin

import io.opentelemetry.context.Context
import io.opentelemetry.context.ContextKey
import io.opentelemetry.context.ContextStorage
import io.opentelemetry.context.StrictContextStorage
import io.opentelemetry.sdk.testing.context.SettableContextStorageProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
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
    fun makeCurrentFails() {
        val context1 = Context.root().with(ANIMAL, "cat")
        assertThat(Context.current().get(ANIMAL)).isNull()
        runBlocking(Dispatchers.Default + context1.asContextElement()) {
            assertThat(Context.current().get(ANIMAL)).isEqualTo("cat")
            assertThatThrownBy {
                context1.makeCurrent()
            }.isInstanceOf(AssertionError::class.java)
        }
    }
}