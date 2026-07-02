/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.provider.TestConfigurableSamplerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.extension.trace.jaeger.sampler.JaegerRemoteSampler;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.io.Closeable;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class TracerProviderConfigurationTest {

  private final SpiHelper spiHelper =
      SpiHelper.create(TracerProviderConfigurationTest.class.getClassLoader());

  @Test
  void configuration() {
    ConfigProperties config =
        DefaultConfigProperties.createFromMap(ImmutableMap.of("test.option", "true"));
    Sampler sampler =
        TracerProviderConfiguration.configureSampler("testSampler", config, spiHelper);

    assertThat(sampler)
        .isInstanceOfSatisfying(
            TestConfigurableSamplerProvider.TestSampler.class,
            s -> assertThat(s.getConfig()).isSameAs(config));
  }

  @Test
  void emptyClassLoader() {
    ConfigProperties config =
        DefaultConfigProperties.createFromMap(ImmutableMap.of("test.option", "true"));
    assertThatThrownBy(
            () ->
                TracerProviderConfiguration.configureSampler(
                    "testSampler", config, SpiHelper.create(new URLClassLoader(new URL[0], null))))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("testSampler");
  }

  @Test
  void samplerNotFound() {
    assertThatThrownBy(
            () ->
                TracerProviderConfiguration.configureSampler(
                    "catSampler",
                    DefaultConfigProperties.createFromMap(Collections.emptyMap()),
                    spiHelper))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("catSampler");
  }

  @Test
  @SuppressLogger(JaegerRemoteSampler.class)
  void configureSampler_JaegerRemoteSampler() throws Exception {
    Sampler sampler =
        TracerProviderConfiguration.configureSampler(
            "parentbased_jaeger_remote",
            DefaultConfigProperties.createFromMap(Collections.emptyMap()),
            spiHelper);
    assertThat(sampler.getClass().getSimpleName()).isEqualTo("ParentBasedSampler");
    assertThat(sampler).extracting("root").isInstanceOf(JaegerRemoteSampler.class);
    // Close the inner JaegerRemoteSampler to shut down its background thread.
    Field rootField = sampler.getClass().getDeclaredField("root");
    rootField.setAccessible(true);
    ((Closeable) rootField.get(sampler)).close();
  }
}
