/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.OpenTelemetrySdkAutoConfigurationCustomizer;
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
final class DefaultOpenTelemetrySdkAutoConfigurationBuilder
    implements io.opentelemetry.sdk.autoconfigure.spi.OpenTelemetrySdkAutoConfigurationBuilder {

  @Nullable private ConfigProperties config;

  private Function<? super TextMapPropagator, ? extends TextMapPropagator> propagatorCustomizer =
      Function.identity();
  private Function<? super SpanExporter, ? extends SpanExporter> spanExporterCustomizer =
      Function.identity();
  private Function<? super Resource, ? extends Resource> resourceCustomizer = Function.identity();
  private Function<? super Sampler, ? extends Sampler> samplerCustomizer = Function.identity();

  private Supplier<Map<String, String>> propertiesSupplier = Collections::emptyMap;

  private boolean setResultAsGlobal = true;

  DefaultOpenTelemetrySdkAutoConfigurationBuilder() {
    for (OpenTelemetrySdkAutoConfigurationCustomizer customizer :
        ServiceLoader.load(OpenTelemetrySdkAutoConfigurationCustomizer.class)) {
      customizer.customize(this);
    }
  }

  @Override
  public DefaultOpenTelemetrySdkAutoConfigurationBuilder setConfig(ConfigProperties config) {
    requireNonNull(config, "config");
    this.config = config;
    return this;
  }

  @Override
  public DefaultOpenTelemetrySdkAutoConfigurationBuilder addPropagatorCustomizer(
      Function<? super TextMapPropagator, ? extends TextMapPropagator> propagatorCustomizer) {
    requireNonNull(propagatorCustomizer, "propagatorCustomizer");
    this.propagatorCustomizer = this.propagatorCustomizer.andThen(propagatorCustomizer);
    return this;
  }

  @Override
  public DefaultOpenTelemetrySdkAutoConfigurationBuilder addResourceCustomizer(
      Function<? super Resource, ? extends Resource> resourceCustomizer) {
    requireNonNull(resourceCustomizer, "resourceCustomizer");
    this.resourceCustomizer = this.resourceCustomizer.andThen(resourceCustomizer);
    return this;
  }

  @Override
  public DefaultOpenTelemetrySdkAutoConfigurationBuilder addSamplerCustomizer(
      Function<? super Sampler, ? extends Sampler> samplerCustomizer) {
    requireNonNull(samplerCustomizer, "samplerCustomizer");
    this.samplerCustomizer = this.samplerCustomizer.andThen(samplerCustomizer);
    return this;
  }

  @Override
  public DefaultOpenTelemetrySdkAutoConfigurationBuilder addSpanExporterCustomizer(
      Function<? super SpanExporter, ? extends SpanExporter> exporterCustomizer) {
    requireNonNull(exporterCustomizer, "exporterCustomizer");
    this.spanExporterCustomizer = this.spanExporterCustomizer.andThen(exporterCustomizer);
    return this;
  }

  @Override
  public DefaultOpenTelemetrySdkAutoConfigurationBuilder addPropertySupplier(
      Supplier<Map<String, String>> propertiesSupplier) {
    requireNonNull(propertiesSupplier, "propertiesSupplier");
    this.propertiesSupplier = mergeProperties(this.propertiesSupplier, propertiesSupplier);
    return this;
  }

  @Override
  public DefaultOpenTelemetrySdkAutoConfigurationBuilder setResultAsGlobal(
      boolean setResultAsGlobal) {
    this.setResultAsGlobal = setResultAsGlobal;
    return this;
  }

  @Override
  public OpenTelemetrySdk newOpenTelemetrySdk() {
    return build().newOpenTelemetrySdk();
  }

  @Override
  public Resource newResource() {
    return build().newResource();
  }

  /**
   * Returns a new {@link OpenTelemetrySdkAutoConfiguration}, configured using the settings of this
   * {@link DefaultOpenTelemetrySdkAutoConfigurationBuilder}.
   */
  private OpenTelemetrySdkAutoConfiguration build() {
    return new OpenTelemetrySdkAutoConfiguration(
        getConfig(),
        propagatorCustomizer,
        spanExporterCustomizer,
        resourceCustomizer,
        samplerCustomizer,
        setResultAsGlobal);
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
