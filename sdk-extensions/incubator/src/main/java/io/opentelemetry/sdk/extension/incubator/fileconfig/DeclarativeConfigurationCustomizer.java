/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import java.util.function.BiFunction;

/** A builder for customizing OpenTelemetry file configuration. */
public interface DeclarativeConfigurationCustomizer {

  /**
   * Adds a {@link BiFunction} to invoke with the declaratively configured {@link Resource} to allow
   * customization. The return value of the {@link BiFunction} will replace the passed-in argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  DeclarativeConfigurationCustomizer addResourceCustomizer(
      BiFunction<? super Resource, DeclarativeConfigProperties, ? extends Resource>
          resourceCustomizer);

  /**
   * Adds a {@link BiFunction} to invoke with the declaratively configured {@link TextMapPropagator}
   * to allow customization. The return value of the {@link BiFunction} will replace the passed-in
   * argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  DeclarativeConfigurationCustomizer addPropagatorCustomizer(
      BiFunction<
              ? super TextMapPropagator, DeclarativeConfigProperties, ? extends TextMapPropagator>
          propagatorCustomizer);

  /**
   * Adds a {@link BiFunction} to invoke with the declaratively configured {@link
   * SdkMeterProviderBuilder} to allow customization. The return value of the {@link BiFunction}
   * will replace the passed-in argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  DeclarativeConfigurationCustomizer addMeterProviderCustomizer(
      BiFunction<
              ? super SdkMeterProviderBuilder,
              DeclarativeConfigProperties,
              ? extends SdkMeterProviderBuilder>
          meterProviderCustomizer);

  /**
   * Adds a {@link BiFunction} to invoke with the declaratively configured {@link
   * SdkTracerProviderBuilder} to allow customization. The return value of the {@link BiFunction}
   * will replace the passed-in argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  DeclarativeConfigurationCustomizer addTraceProviderCustomizer(
      BiFunction<
              ? super SdkTracerProviderBuilder,
              DeclarativeConfigProperties,
              ? extends SdkTracerProviderBuilder>
          traceProviderCustomizer);

  /**
   * Adds a {@link BiFunction} to invoke with the declaratively configured {@link
   * SdkLoggerProviderBuilder} to allow customization. The return value of the {@link BiFunction}
   * will replace the passed-in argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  DeclarativeConfigurationCustomizer addLoggerProviderCustomizer(
      BiFunction<
              ? super SdkLoggerProviderBuilder,
              DeclarativeConfigProperties,
              ? extends SdkLoggerProviderBuilder>
          loggerProviderCustomizer);
}
