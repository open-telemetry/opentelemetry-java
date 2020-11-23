package io.opentelemetry.extension.kotlin

import io.opentelemetry.context.Context
import io.opentelemetry.context.ImplicitContextKeyed
import kotlin.coroutines.CoroutineContext

/**
 * Returns a [CoroutineContext] which will make this [Context] current when resuming a coroutine
 * and restores the previous [Context] on suspension.
 */
fun Context.asContextElement(): CoroutineContext {
    return KotlinContextElement(this)
}

/**
 * Returns a [CoroutineContext] which will make this [ImplicitContextKeyed] current when resuming a
 * coroutine and restores the previous [Context] on suspension.
 */
fun ImplicitContextKeyed.asContextElement(): CoroutineContext {
    return KotlinContextElement(Context.current().with(this))
}

/**
 * Returns the [Context] in this [CoroutineContext] if present, or the root otherwise.
 */
fun CoroutineContext.getOpenTelemetryContext(): Context {
    val element = get(KotlinContextElement.KEY)
    if (element is KotlinContextElement) {
        return element.context
    }
    return Context.root()
}
