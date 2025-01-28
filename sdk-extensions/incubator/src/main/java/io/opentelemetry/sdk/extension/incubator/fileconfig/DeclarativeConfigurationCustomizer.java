/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import java.util.function.BiFunction;

/** A builder for customizing OpenTelemetry declarative configuration. */
public interface DeclarativeConfigurationCustomizer {

  /**
   * Adds a {@link BiFunction} to invoke with the declaratively configured {@link TextMapPropagator}
   * to allow customization. The return value of the {@link BiFunction} will replace the passed-in
   * argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  DeclarativeConfigurationCustomizer addPropagatorCustomizer(
      BiFunction<? super TextMapPropagator, StructuredConfigProperties, ? extends TextMapPropagator>
          propagatorCustomizer);
}
