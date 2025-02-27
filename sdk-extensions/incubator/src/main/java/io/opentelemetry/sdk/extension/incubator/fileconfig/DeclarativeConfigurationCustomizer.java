/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import java.util.function.BiFunction;

/** A builder for customizing OpenTelemetry file configuration. */
public interface DeclarativeConfigurationCustomizer {

  /**
   * Adds a {@link BiFunction} to invoke with the declaratively configured {@link TextMapPropagator}
   * to allow customization.
   */
  DeclarativeConfigurationCustomizer addPropagatorCustomizer(
      BiFunction<? super TextMapPropagator, StructuredConfigProperties, ? extends TextMapPropagator>
          propagatorCustomizer);
}
