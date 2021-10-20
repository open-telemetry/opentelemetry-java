/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi;

import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/** A builder for customizing OpenTelemetry auto-configuration. */
public interface AutoConfiguredOpenTelemetrySdkCustomizer {

  /**
   * Adds a {@link Function} to invoke with the default autoconfigured {@link TextMapPropagator} to
   * allow customization. The return value of the {@link Function} will replace the passed-in
   * argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  AutoConfiguredOpenTelemetrySdkCustomizer addPropagatorCustomizer(
      SdkComponentCustomizer<? super TextMapPropagator, ? extends TextMapPropagator>
          propagatorCustomizer);

  /**
   * Adds a {@link Function} to invoke with the default autoconfigured {@link TextMapPropagator} to
   * allow customization. The return value of the {@link Function} will replace the passed-in
   * argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  AutoConfiguredOpenTelemetrySdkCustomizer addResourceCustomizer(
      SdkComponentCustomizer<? super Resource, ? extends Resource> resourceCustomizer);

  /**
   * Adds a {@link Function} to invoke with the default autoconfigured {@link TextMapPropagator} to
   * allow customization. The return value of the {@link Function} will replace the passed-in
   * argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  AutoConfiguredOpenTelemetrySdkCustomizer addSamplerCustomizer(
      SdkComponentCustomizer<? super Sampler, ? extends Sampler> samplerCustomizer);

  /**
   * Adds a {@link Function} to invoke with the default autoconfigured {@link SpanExporter} to allow
   * customization. The return value of the {@link Function} will replace the passed-in argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  AutoConfiguredOpenTelemetrySdkCustomizer addSpanExporterCustomizer(
      SdkComponentCustomizer<? super SpanExporter, ? extends SpanExporter> exporterCustomizer);

  /**
   * Adds a {@link Supplier} of a map of property names and values to use as defaults for the {@link
   * ConfigProperties} used during auto-configuration. The order of precedence of properties is
   * system properties > environment variables > the suppliers registered with this method.
   *
   * <p>Multiple calls will cause properties to be merged in order, with later ones overwriting
   * duplicate keys in earlier ones.
   */
  AutoConfiguredOpenTelemetrySdkCustomizer addPropertySupplier(
      Supplier<Map<String, String>> propertiesSupplier);
}
