/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfiguredSdkCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfiguredSdkCustomizerProvider;
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
public final class AutoConfiguredSdkBuilder implements AutoConfiguredSdkCustomizer {

  @Nullable private ConfigProperties config;

  private Function<? super TextMapPropagator, ? extends TextMapPropagator> propagatorCustomizer =
      Function.identity();
  private Function<? super SpanExporter, ? extends SpanExporter> spanExporterCustomizer =
      Function.identity();
  private Function<? super Resource, ? extends Resource> resourceCustomizer = Function.identity();
  private Function<? super Sampler, ? extends Sampler> samplerCustomizer = Function.identity();

  private Supplier<Map<String, String>> propertiesSupplier = Collections::emptyMap;

  private boolean setResultAsGlobal = true;

  AutoConfiguredSdkBuilder() {
    for (AutoConfiguredSdkCustomizerProvider customizer :
        ServiceLoader.load(AutoConfiguredSdkCustomizerProvider.class)) {
      customizer.customize(this);
    }
  }

  @Override
  public AutoConfiguredSdkBuilder setConfig(ConfigProperties config) {
    requireNonNull(config, "config");
    this.config = config;
    return this;
  }

  @Override
  public AutoConfiguredSdkBuilder addPropagatorCustomizer(
      Function<? super TextMapPropagator, ? extends TextMapPropagator> propagatorCustomizer) {
    requireNonNull(propagatorCustomizer, "propagatorCustomizer");
    this.propagatorCustomizer = this.propagatorCustomizer.andThen(propagatorCustomizer);
    return this;
  }

  @Override
  public AutoConfiguredSdkBuilder addResourceCustomizer(
      Function<? super Resource, ? extends Resource> resourceCustomizer) {
    requireNonNull(resourceCustomizer, "resourceCustomizer");
    this.resourceCustomizer = this.resourceCustomizer.andThen(resourceCustomizer);
    return this;
  }

  @Override
  public AutoConfiguredSdkBuilder addSamplerCustomizer(
      Function<? super Sampler, ? extends Sampler> samplerCustomizer) {
    requireNonNull(samplerCustomizer, "samplerCustomizer");
    this.samplerCustomizer = this.samplerCustomizer.andThen(samplerCustomizer);
    return this;
  }

  @Override
  public AutoConfiguredSdkBuilder addSpanExporterCustomizer(
      Function<? super SpanExporter, ? extends SpanExporter> exporterCustomizer) {
    requireNonNull(exporterCustomizer, "exporterCustomizer");
    this.spanExporterCustomizer = this.spanExporterCustomizer.andThen(exporterCustomizer);
    return this;
  }

  @Override
  public AutoConfiguredSdkBuilder addPropertySupplier(
      Supplier<Map<String, String>> propertiesSupplier) {
    requireNonNull(propertiesSupplier, "propertiesSupplier");
    this.propertiesSupplier = mergeProperties(this.propertiesSupplier, propertiesSupplier);
    return this;
  }

  @Override
  public AutoConfiguredSdkBuilder setResultAsGlobal(boolean setResultAsGlobal) {
    this.setResultAsGlobal = setResultAsGlobal;
    return this;
  }

  /**
   * Returns a new {@link AutoConfiguredSdk} holding components auto-configured using the settings
   * of this {@link AutoConfiguredSdkBuilder}.
   */
  @SuppressWarnings("deprecation") // Using classes which will be made package-private later.
  public AutoConfiguredSdk build() {
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
    return AutoConfiguredSdk.create(sdk, resource);
  }

  // Visible for testing
  ConfigProperties getConfig() {
    ConfigProperties config = this.config;
    if (config == null) {
      config = DefaultConfigProperties.get(propertiesSupplier.get());
    }
    return config;
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
