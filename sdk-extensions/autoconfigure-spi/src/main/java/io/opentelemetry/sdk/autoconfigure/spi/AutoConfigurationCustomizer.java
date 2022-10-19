/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi;

import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/** A builder for customizing OpenTelemetry auto-configuration. */
public interface AutoConfigurationCustomizer {

  /**
   * Adds a {@link BiFunction} to invoke with the default autoconfigured {@link TextMapPropagator}
   * to allow customization. The return value of the {@link BiFunction} will replace the passed-in
   * argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  AutoConfigurationCustomizer addPropagatorCustomizer(
      BiFunction<? super TextMapPropagator, ConfigProperties, ? extends TextMapPropagator>
          propagatorCustomizer);

  /**
   * Adds a {@link BiFunction} to invoke with the default autoconfigured {@link Resource} to allow
   * customization. The return value of the {@link BiFunction} will replace the passed-in argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  AutoConfigurationCustomizer addResourceCustomizer(
      BiFunction<? super Resource, ConfigProperties, ? extends Resource> resourceCustomizer);

  /**
   * Adds a {@link BiFunction} to invoke with the default autoconfigured {@link Sampler} to allow
   * customization. The return value of the {@link BiFunction} will replace the passed-in argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  AutoConfigurationCustomizer addSamplerCustomizer(
      BiFunction<? super Sampler, ConfigProperties, ? extends Sampler> samplerCustomizer);

  /**
   * Adds a {@link BiFunction} to invoke with the default autoconfigured {@link SpanExporter} to
   * allow customization. The return value of the {@link BiFunction} will replace the passed-in
   * argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  AutoConfigurationCustomizer addSpanExporterCustomizer(
      BiFunction<? super SpanExporter, ConfigProperties, ? extends SpanExporter>
          exporterCustomizer);

  /**
   * Adds a {@link Supplier} of a map of property names and values to use as defaults for the {@link
   * ConfigProperties} used during auto-configuration. The order of precedence of properties is
   * system properties > environment variables > the suppliers registered with this method.
   *
   * <p>Multiple calls will cause properties to be merged in order, with later ones overwriting
   * duplicate keys in earlier ones.
   */
  AutoConfigurationCustomizer addPropertiesSupplier(
      Supplier<Map<String, String>> propertiesSupplier);

  /**
   * Adds a {@link Function} to invoke the with the {@link ConfigProperties} to allow customization.
   * The return value of the {@link Function} will be merged into the {@link ConfigProperties}
   * before it is used for auto-configuration, overwriting the properties that are already there.
   *
   * <p>Multiple calls will cause properties to be merged in order, with later ones overwriting
   * duplicate keys in earlier ones.
   *
   * @since 1.17.0
   */
  default AutoConfigurationCustomizer addPropertiesCustomizer(
      Function<ConfigProperties, Map<String, String>> propertiesCustomizer) {
    return this;
  }

  /**
   * Adds a {@link BiFunction} to invoke the with the {@link SdkTracerProviderBuilder} to allow
   * customization. The return value of the {@link BiFunction} will replace the passed-in argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   *
   * <p>Note: calling {@link SdkTracerProviderBuilder#setSampler(Sampler)} inside of your
   * configuration function will cause any sampler customizers to be ignored that were configured
   * via {@link #addSamplerCustomizer(BiFunction)}. If you want to replace the default sampler,
   * check out {@link io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSamplerProvider} and
   * use {@link #addPropertiesSupplier(Supplier)} to set `otel.traces.sampler` to your named
   * sampler.
   *
   * <p>Similarly, calling {@link SdkTracerProviderBuilder#setResource(Resource)} inside of your
   * configuration function will cause any resource customizers to be ignored that were configured
   * via {@link #addResourceCustomizer(BiFunction)}.
   */
  default AutoConfigurationCustomizer addTracerProviderCustomizer(
      BiFunction<SdkTracerProviderBuilder, ConfigProperties, SdkTracerProviderBuilder>
          tracerProviderCustomizer) {
    return this;
  }

  /**
   * Adds a {@link BiFunction} to invoke the with the {@link SdkMeterProviderBuilder} to allow
   * customization. The return value of the {@link BiFunction} will replace the passed-in argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  default AutoConfigurationCustomizer addMeterProviderCustomizer(
      BiFunction<SdkMeterProviderBuilder, ConfigProperties, SdkMeterProviderBuilder>
          meterProviderCustomizer) {
    return this;
  }

  /**
   * Adds a {@link BiFunction} to invoke with the default autoconfigured {@link MetricExporter} to
   * allow customization. The return value of the {@link BiFunction} will replace the passed-in
   * argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  default AutoConfigurationCustomizer addMetricExporterCustomizer(
      BiFunction<? super MetricExporter, ConfigProperties, ? extends MetricExporter>
          exporterCustomizer) {
    return this;
  }

  /**
   * Adds a {@link BiFunction} to invoke the with the {@link SdkLoggerProviderBuilder} to allow
   * customization. The return value of the {@link BiFunction} will replace the passed-in argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   *
   * @since 1.19.0
   */
  default AutoConfigurationCustomizer addLoggerProviderCustomizer(
      BiFunction<SdkLoggerProviderBuilder, ConfigProperties, SdkLoggerProviderBuilder>
          meterProviderCustomizer) {
    return this;
  }

  /**
   * Adds a {@link BiFunction} to invoke with the default autoconfigured {@link LogRecordExporter}
   * to allow customization. The return value of the {@link BiFunction} will replace the passed-in
   * argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   *
   * @since 1.19.0
   */
  default AutoConfigurationCustomizer addLogRecordExporterCustomizer(
      BiFunction<? super LogRecordExporter, ConfigProperties, ? extends LogRecordExporter>
          exporterCustomizer) {
    return this;
  }
}
