/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context.propagation;

/**
 * {@code DefaultContextPropagators} is the default, built-in implementation of {@link
 * ContextPropagators}.
 *
 * <p>All the registered propagators are stored internally as a simple list, and are invoked
 * synchronically upon injection and extraction.
 *
 * <p>The propagation fields retrieved from all registered propagators are de-duplicated.
 */
final class DefaultContextPropagators implements ContextPropagators {

  private static final ContextPropagators NOOP =
      new DefaultContextPropagators(NoopTextMapPropagator.getInstance());

  static ContextPropagators noop() {
    return NOOP;
  }

  private final TextMapPropagator textMapPropagator;

  @Override
  public TextMapPropagator getTextMapPropagator() {
    return textMapPropagator;
  }

  DefaultContextPropagators(TextMapPropagator textMapPropagator) {
    this.textMapPropagator = textMapPropagator;
  }
}
