/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class ConfigurableSamplerTest {

  @Test
  void configuration() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(ImmutableMap.of("test.option", "true"));
    Sampler sampler =
        TracerProviderConfiguration.configureSampler(
            "testSampler", config, TracerProviderConfiguration.class.getClassLoader());

    assertThat(sampler)
        .isInstanceOfSatisfying(
            TestConfigurableSamplerProvider.TestSampler.class,
            s -> assertThat(s.getConfig()).isSameAs(config));
  }

  @Test
  void emptyClassLoader() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(ImmutableMap.of("test.option", "true"));
    assertThatThrownBy(
            () ->
                TracerProviderConfiguration.configureSampler(
                    "testSampler", config, new URLClassLoader(new URL[0], null)))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("testSampler");
  }

  @Test
  void samplerNotFound() {
    assertThatThrownBy(
            () ->
                TracerProviderConfiguration.configureSampler(
                    "catSampler",
                    DefaultConfigProperties.createForTest(Collections.emptyMap()),
                    TracerProviderConfiguration.class.getClassLoader()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("catSampler");
  }
}
