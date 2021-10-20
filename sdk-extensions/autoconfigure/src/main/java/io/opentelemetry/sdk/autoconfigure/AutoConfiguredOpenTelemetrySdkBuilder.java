/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfiguredOpenTelemetrySdkCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfiguredOpenTelemetrySdkCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * A builder for configuring auto-configuration of the OpenTelemetry SDK. Notably, auto-configured
 * components can be customized, for example by delegating to them from a wrapper that tweaks
 * behavior such as filtering out telemetry attributes.
 */
public final class AutoConfiguredOpenTelemetrySdkBuilder
    implements AutoConfiguredOpenTelemetrySdkCustomizer {

  @Nullable private ConfigProperties config;

  private BiFunction<? super TextMapPropagator, ConfigProperties, ? extends TextMapPropagator>
      propagatorCustomizer = (a, unused) -> a;
  private BiFunction<? super SpanExporter, ConfigProperties, ? extends SpanExporter>
      spanExporterCustomizer = (a, unused) -> a;
  private BiFunction<? super Resource, ConfigProperties, ? extends Resource> resourceCustomizer =
      (a, unused) -> a;
  private BiFunction<? super Sampler, ConfigProperties, ? extends Sampler> samplerCustomizer =
      (a, unused) -> a;

  private Supplier<Map<String, String>> propertiesSupplier = Collections::emptyMap;

  private boolean setResultAsGlobal = true;

  AutoConfiguredOpenTelemetrySdkBuilder() {
    for (AutoConfiguredOpenTelemetrySdkCustomizerProvider customizer :
        ServiceLoader.load(AutoConfiguredOpenTelemetrySdkCustomizerProvider.class)) {
      customizer.customize(this);
    }
  }

  /**
   * Sets the {@link ConfigProperties} to use when resolving properties for auto-configuration.
   * {@link #addPropertiesSupplier(Supplier)} will have no effect if this method is used.
   */
  public AutoConfiguredOpenTelemetrySdkBuilder setConfig(ConfigProperties config) {
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
  @Override
  public AutoConfiguredOpenTelemetrySdkBuilder addPropagatorCustomizer(
      BiFunction<? super TextMapPropagator, ConfigProperties, ? extends TextMapPropagator>
          propagatorCustomizer) {
    requireNonNull(propagatorCustomizer, "propagatorCustomizer");
    this.propagatorCustomizer = mergeCustomizer(this.propagatorCustomizer, propagatorCustomizer);
    return this;
  }

  /**
   * Adds a {@link Function} to invoke with the default autoconfigured {@link TextMapPropagator} to
   * allow customization. The return value of the {@link Function} will replace the passed-in
   * argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  @Override
  public AutoConfiguredOpenTelemetrySdkBuilder addResourceCustomizer(
      BiFunction<? super Resource, ConfigProperties, ? extends Resource> resourceCustomizer) {
    requireNonNull(resourceCustomizer, "resourceCustomizer");
    this.resourceCustomizer = mergeCustomizer(this.resourceCustomizer, resourceCustomizer);
    return this;
  }

  /**
   * Adds a {@link Function} to invoke with the default autoconfigured {@link TextMapPropagator} to
   * allow customization. The return value of the {@link Function} will replace the passed-in
   * argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  @Override
  public AutoConfiguredOpenTelemetrySdkBuilder addSamplerCustomizer(
      BiFunction<? super Sampler, ConfigProperties, ? extends Sampler> samplerCustomizer) {
    requireNonNull(samplerCustomizer, "samplerCustomizer");
    this.samplerCustomizer = mergeCustomizer(this.samplerCustomizer, samplerCustomizer);
    return this;
  }

  /**
   * Adds a {@link Function} to invoke with the default autoconfigured {@link SpanExporter} to allow
   * customization. The return value of the {@link Function} will replace the passed-in argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  @Override
  public AutoConfiguredOpenTelemetrySdkBuilder addSpanExporterCustomizer(
      BiFunction<? super SpanExporter, ConfigProperties, ? extends SpanExporter>
          spanExporterCustomizer) {
    requireNonNull(spanExporterCustomizer, "spanExporterCustomizer");
    this.spanExporterCustomizer =
        mergeCustomizer(this.spanExporterCustomizer, spanExporterCustomizer);
    return this;
  }

  /**
   * Adds a {@link Supplier} of a map of property names and values to use as defaults for the {@link
   * ConfigProperties} used during auto-configuration. The order of precedence of properties is
   * system properties > environment variables > the suppliers registered with this method.
   *
   * <p>Multiple calls will cause properties to be merged in order, with later ones overwriting
   * duplicate keys in earlier ones.
   */
  @Override
  public AutoConfiguredOpenTelemetrySdkBuilder addPropertiesSupplier(
      Supplier<Map<String, String>> propertiesSupplier) {
    requireNonNull(propertiesSupplier, "propertiesSupplier");
    this.propertiesSupplier = mergeProperties(this.propertiesSupplier, propertiesSupplier);
    return this;
  }

  /**
   * Sets whether the configured {@link OpenTelemetrySdk} should be set as the application's
   * {@linkplain io.opentelemetry.api.GlobalOpenTelemetry global} instance.
   */
  public AutoConfiguredOpenTelemetrySdkBuilder setResultAsGlobal(boolean setResultAsGlobal) {
    this.setResultAsGlobal = setResultAsGlobal;
    return this;
  }

  /**
   * Returns a new {@link AutoConfiguredOpenTelemetrySdk} holding components auto-configured using
   * the settings of this {@link AutoConfiguredOpenTelemetrySdkBuilder}.
   */
  @SuppressWarnings("deprecation") // Using classes which will be made package-private later.
  public AutoConfiguredOpenTelemetrySdk build() {
    ConfigProperties config = getConfig();
    Resource resource =
        OpenTelemetryResourceAutoConfiguration.configureResource(config, resourceCustomizer);
    OpenTelemetrySdk sdk =
        OpenTelemetrySdkAutoConfiguration.newOpenTelemetrySdk(
            config,
            resource,
            propagatorCustomizer,
            spanExporterCustomizer,
            samplerCustomizer,
            setResultAsGlobal);
    return AutoConfiguredOpenTelemetrySdk.create(sdk, resource);
  }

  // Visible for testing
  ConfigProperties getConfig() {
    ConfigProperties config = this.config;
    if (config == null) {
      config = DefaultConfigProperties.get(propertiesSupplier.get());
    }
    return config;
  }

  private static <I, O1, O2> BiFunction<I, ConfigProperties, O2> mergeCustomizer(
      BiFunction<? super I, ConfigProperties, ? extends O1> first,
      BiFunction<? super O1, ConfigProperties, ? extends O2> second) {
    return (I configured, ConfigProperties config) -> {
      O1 firstResult = first.apply(configured, config);
      return second.apply(firstResult, config);
    };
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
