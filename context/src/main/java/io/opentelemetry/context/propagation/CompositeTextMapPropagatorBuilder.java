/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context.propagation;

import java.util.ArrayList;
import java.util.List;

/**
 * A builder for configuring an {@link TextMapPropagator} specifying which propagators should be
 * used for extracting the context and which should be used for injecting the context.
 */
public final class CompositeTextMapPropagatorBuilder {

  private final List<TextMapPropagator> extractors;
  private final List<TextMapPropagator> injectors;

  /**
   * Package protected to disallow direct initialization.
   *
   * @see TextMapPropagator#builder()
   */
  CompositeTextMapPropagatorBuilder() {
    this.extractors = new ArrayList<>();
    this.injectors = new ArrayList<>();
  }

  /**
   * Add a {@link TextMapPropagator} to be used only to extract the context.
   */
  public CompositeTextMapPropagatorBuilder extractor(TextMapPropagator propagator) {
    this.extractors.add(propagator);
    return this;
  }

  /**
   * Add a {@link TextMapPropagator} to be used only to inject the context.
   */
  public CompositeTextMapPropagatorBuilder injector(TextMapPropagator propagator) {
    this.injectors.add(propagator);
    return this;
  }

  /**
   * Add a {@link TextMapPropagator} to be used both to extract and inject the context.
   */
  public CompositeTextMapPropagatorBuilder propagator(TextMapPropagator propagator) {
    this.injectors.add(propagator);
    this.extractors.add(propagator);
    return this;
  }

  /**
   * Returns the built {@link TextMapPropagator}.
   *
   * @see CompositeTextMapPropagatorBuilder
   */
  public TextMapPropagator build() {
    if (this.injectors.isEmpty()) {
      this.injectors.add(TextMapPropagator.noop());
    }
    if (this.extractors.isEmpty()) {
      this.extractors.add(TextMapPropagator.noop());
    }

    return new MultiTextMapPropagator(this.extractors, this.injectors);
  }
}
