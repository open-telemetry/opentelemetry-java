/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.opentracing.shim.internal.OtelVersion;
import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtract;
import io.opentracing.propagation.TextMapInject;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

final class TracerShim implements Tracer {
  private static final Logger logger = Logger.getLogger(TracerShim.class.getName());

  private final io.opentelemetry.api.trace.TracerProvider provider;
  private final io.opentelemetry.api.trace.Tracer tracer;
  private final ScopeManager scopeManagerShim;
  private final Propagation propagation;
  private final AtomicBoolean isShutdown = new AtomicBoolean();

  TracerShim(
      io.opentelemetry.api.trace.TracerProvider provider,
      TextMapPropagator textMapPropagator,
      TextMapPropagator httpPropagator) {
    this.provider = provider;
    this.tracer = provider.get("opentracing-shim", OtelVersion.VERSION);
    this.propagation = new Propagation(textMapPropagator, httpPropagator);
    this.scopeManagerShim = new ScopeManagerShim();
  }

  // Visible for testing
  io.opentelemetry.api.trace.Tracer tracer() {
    return tracer;
  }

  // Visible for testing
  Propagation propagation() {
    return propagation;
  }

  @Override
  public ScopeManager scopeManager() {
    return scopeManagerShim;
  }

  @Override
  public Span activeSpan() {
    return scopeManagerShim.activeSpan();
  }

  @Override
  public Scope activateSpan(Span span) {
    return scopeManagerShim.activate(span);
  }

  @Override
  public SpanBuilder buildSpan(String operationName) {
    if (isShutdown.get()) {
      return new NoopSpanBuilderShim(operationName);
    }

    return new SpanBuilderShim(tracer, operationName);
  }

  @Override
  public <C> void inject(SpanContext context, Format<C> format, C carrier) {
    if (context == null) {
      logger.log(Level.WARNING, "Cannot inject a null span context.");
      return;
    }

    SpanContextShim contextShim = ShimUtil.getContextShim(context);
    if (contextShim == null) {
      logger.log(Level.WARNING, "Cannot inject a null span context shim.");
      return;
    }

    if (format == Format.Builtin.TEXT_MAP
        || format == Format.Builtin.TEXT_MAP_INJECT
        || format == Format.Builtin.HTTP_HEADERS) {
      propagation.injectTextMap(contextShim, format, (TextMapInject) carrier);
    }
  }

  @Override
  @Nullable
  public <C> SpanContext extract(Format<C> format, C carrier) {
    try {
      if (format == Format.Builtin.TEXT_MAP
          || format == Format.Builtin.TEXT_MAP_EXTRACT
          || format == Format.Builtin.HTTP_HEADERS) {
        return propagation.extractTextMap(format, (TextMapExtract) carrier);
      }
    } catch (RuntimeException e) {
      logger.log(
          Level.WARNING,
          "Exception caught while extracting span context; returning null. "
              + "Exception: [{0}] Message: [{1}]",
          new String[] {e.getClass().getName(), e.getMessage()});
    }

    return null;
  }

  @Override
  public void close() {
    if (!isShutdown.compareAndSet(false, true)) {
      return;
    }

    TracerProvider provider = maybeUnobfuscate(this.provider);
    if (provider instanceof Closeable) {
      try {
        ((Closeable) provider).close();
      } catch (RuntimeException | IOException e) {
        logger.log(Level.WARNING, "Exception caught while closing TracerProvider.", e);
      }
    }
  }

  private static TracerProvider maybeUnobfuscate(TracerProvider tracerProvider) {
    if (!tracerProvider.getClass().getSimpleName().equals("ObfuscatedTracerProvider")) {
      return tracerProvider;
    }
    try {
      Field delegateField = tracerProvider.getClass().getDeclaredField("delegate");
      delegateField.setAccessible(true);
      Object delegate = delegateField.get(tracerProvider);
      if (delegate instanceof TracerProvider) {
        return (TracerProvider) delegate;
      }
    } catch (NoSuchFieldException | IllegalAccessException e) {
      logger.log(Level.INFO, "Error trying to unobfuscate SdkTracerProvider", e);
    }
    return tracerProvider;
  }
}
