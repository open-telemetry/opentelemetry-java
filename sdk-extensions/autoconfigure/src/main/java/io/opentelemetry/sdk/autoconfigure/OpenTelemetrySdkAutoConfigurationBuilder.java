/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * A builder for configuring auto-configuration of the OpenTelemetry SDK. Notably, auto-configured
 * components can be customized, for example by delegating to them from a wrapper that tweaks
 * behavior such as filtering out telemetry attributes.
 */
public final class OpenTelemetrySdkAutoConfigurationBuilder {

  @Nullable private ConfigProperties config;

  private Function<? super TextMapPropagator, ? extends TextMapPropagator> propagatorCustomizer =
      Function.identity();
  private Function<? super SpanExporter, ? extends SpanExporter> spanExporterCustomizer =
      Function.identity();
  private Function<? super Resource, ? extends Resource> resourceCustomizer = Function.identity();
  private Function<? super Sampler, ? extends Sampler> samplerCustomizer = Function.identity();

  private Supplier<Map<String, String>> propertiesSupplier = Collections::emptyMap;

  private boolean setResultAsGlobal = true;

  /**
   * Sets the {@link ConfigProperties} to use when resolving properties for auto-configuration.
   * {@link #addPropertySupplier(Supplier)} will have no effect if this method is used.
   */
  public OpenTelemetrySdkAutoConfigurationBuilder setConfig(ConfigProperties config) {
    requireNonNull(config, "config");
    this.config = config;
    return this;
  }

  /**
   * Adds a {@link Function} to invoke with the default autoconfigured {@link TextMapPropagator} to
   * allow customization. The return value of the {@link Function} will replace the passed-in
   * argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  public OpenTelemetrySdkAutoConfigurationBuilder addPropagatorCustomizer(
      Function<? super TextMapPropagator, ? extends TextMapPropagator> propagatorCustomizer) {
    requireNonNull(propagatorCustomizer, "propagatorCustomizer");
    this.propagatorCustomizer = this.propagatorCustomizer.andThen(propagatorCustomizer);
    return this;
  }

  /**
   * Adds a {@link Function} to invoke with the default autoconfigured {@link TextMapPropagator} to
   * allow customization. The return value of the {@link Function} will replace the passed-in
   * argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  public OpenTelemetrySdkAutoConfigurationBuilder addResourceCustomizer(
      Function<? super Resource, ? extends Resource> resourceCustomizer) {
    requireNonNull(resourceCustomizer, "resourceCustomizer");
    this.resourceCustomizer = this.resourceCustomizer.andThen(resourceCustomizer);
    return this;
  }

  /**
   * Adds a {@link Function} to invoke with the default autoconfigured {@link TextMapPropagator} to
   * allow customization. The return value of the {@link Function} will replace the passed-in
   * argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  public OpenTelemetrySdkAutoConfigurationBuilder addSamplerCustomizer(
      Function<? super Sampler, ? extends Sampler> samplerCustomizer) {
    requireNonNull(samplerCustomizer, "samplerCustomizer");
    this.samplerCustomizer = this.samplerCustomizer.andThen(samplerCustomizer);
    return this;
  }

  /**
   * Adds a {@link Function} to invoke with the default autoconfigured {@link SpanExporter} to allow
   * customization. The return value of the {@link Function} will replace the passed-in argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  public OpenTelemetrySdkAutoConfigurationBuilder addSpanExporterCustomizer(
      Function<? super SpanExporter, ? extends SpanExporter> exporterCustomizer) {
    requireNonNull(exporterCustomizer, "exporterCustomizer");
    this.spanExporterCustomizer = this.spanExporterCustomizer.andThen(exporterCustomizer);
    return this;
  }

  /**
   * Adds a {@link Supplier} of a map of property names and values to use as defaults for the {@link
   * io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties} used during auto-configuration. The
   * order of precedence of properties is system properties > environment variables > the suppliers
   * registered with this method.
   *
   * <p>Multiple calls will cause properties to be merged in order, with later ones overwriting
   * duplicate keys in earlier ones.
   */
  public OpenTelemetrySdkAutoConfigurationBuilder addPropertySupplier(
      Supplier<Map<String, String>> propertiesSupplier) {
    requireNonNull(propertiesSupplier, "propertiesSupplier");
    this.propertiesSupplier = mergeProperties(this.propertiesSupplier, propertiesSupplier);
    return this;
  }

  /**
   * Sets whether the configured {@link OpenTelemetrySdk} should be set as the application's
   * {@linkplain io.opentelemetry.api.GlobalOpenTelemetry global} instance.
   */
  public OpenTelemetrySdkAutoConfigurationBuilder setResultAsGlobal(boolean setResultAsGlobal) {
    this.setResultAsGlobal = setResultAsGlobal;
    return this;
  }

  /**
   * Returns a new {@link OpenTelemetrySdkAutoConfiguration}, configured using the settings of this
   * {@link OpenTelemetrySdkAutoConfigurationBuilder}.
   */
  public OpenTelemetrySdkAutoConfiguration build() {
    for (OpenTelemetrySdkAutoConfigurationCustomizer customizer :
        ServiceLoader.load(OpenTelemetrySdkAutoConfigurationCustomizer.class)) {
      customizer.customize(this);
    }

    ConfigProperties config = this.config;
    if (config == null) {
      config = DefaultConfigProperties.get(propertiesSupplier.get());
    }
    return new OpenTelemetrySdkAutoConfiguration(
        config,
        propagatorCustomizer,
        spanExporterCustomizer,
        resourceCustomizer,
        samplerCustomizer,
        setResultAsGlobal);
  }

  private static Supplier<Map<String, String>> mergeProperties(
      Supplier<Map<String, String>> first, Supplier<Map<String, String>> second) {
    return () -> {
      Map<String, String> merged = new HashMap<>();
      merged.putAll(first.get());
      merged.putAll(second.get());
      return merged;
    };
  }
}
