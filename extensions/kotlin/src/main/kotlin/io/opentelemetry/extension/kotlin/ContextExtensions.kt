package io.opentelemetry.extension.kotlin

import io.opentelemetry.api.trace.Span
import io.opentelemetry.context.Context
import io.opentelemetry.context.ImplicitContextKeyed
import kotlin.coroutines.CoroutineContext

/**
 * Returns a [CoroutineContext] which will make this [Context] current when resuming a coroutine
 * and restores the previous [Context] on suspension.
 */
fun Context.asContextElement(): CoroutineContext {
    return ContextElement(this)
}

/**
 * Returns a [CoroutineContext] which will make this [ImplicitContextKeyed] current when resuming a
 * coroutine and restores the previous [Context] on suspension.
 */
fun ImplicitContextKeyed.asContextElement(): CoroutineContext {
    return ContextElement(Context.current().with(this))
}

/**
 * Returns the [Context] in this [CoroutineContext] if present, or the root otherwise.
 */
fun CoroutineContext.getOpenTelemetryContext(): Context {
    val element = get(ContextElement.KEY)
    if (element is ContextElement) {
        return element.context
    }
    return Context.root()
}
