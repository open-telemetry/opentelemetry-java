/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.kotlin;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.ThreadContextElement;
import org.jetbrains.annotations.Nullable;

/**
 * {@link ThreadContextElement} for synchronizing a {@link Context} across coroutine suspension and
 * resumption. Implemented in Java instead of Kotlin to allow usage in auto-instrumentation where
 * there is an outstanding Kotlin bug preventing it https://youtrack.jetbrains.com/issue/KT-20869.
 */
class KotlinContextElement implements ThreadContextElement<Scope> {

  static final CoroutineContext.Key<KotlinContextElement> KEY =
      new CoroutineContext.Key<KotlinContextElement>() {};

  private final Context otelContext;

  KotlinContextElement(Context otelContext) {
    this.otelContext = otelContext;
  }

  Context getContext() {
    return otelContext;
  }

  @Override
  public CoroutineContext.Key<?> getKey() {
    return KEY;
  }

  @Override
  @SuppressWarnings("MustBeClosedChecker")
  public Scope updateThreadContext(CoroutineContext coroutineContext) {
    return otelContext.makeCurrent();
  }

  @Override
  public void restoreThreadContext(CoroutineContext coroutineContext, Scope scope) {
    scope.close();
  }

  @Override
  public CoroutineContext plus(CoroutineContext coroutineContext) {
    return CoroutineContext.DefaultImpls.plus(this, coroutineContext);
  }

  @Override
  public <R> R fold(
      R initial, Function2<? super R, ? super CoroutineContext.Element, ? extends R> operation) {
    return CoroutineContext.Element.DefaultImpls.fold(this, initial, operation);
  }

  @Nullable
  @Override
  public <E extends CoroutineContext.Element> E get(CoroutineContext.Key<E> key) {
    return CoroutineContext.Element.DefaultImpls.get(this, key);
  }

  @Override
  public CoroutineContext minusKey(CoroutineContext.Key<?> key) {
    return CoroutineContext.Element.DefaultImpls.minusKey(this, key);
  }
}
