/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.extension.trace.jaeger.sampler.JaegerRemoteSampler;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class ConfigurableSamplerTest {

  private static final Resource EMPTY_RESOURCE = Resource.empty();

  @Test
  void configuration() {
    ConfigProperties config =
        ConfigProperties.createForTest(ImmutableMap.of("test.option", "true"));
    Sampler sampler =
        TracerProviderConfiguration.configureSampler("testSampler", EMPTY_RESOURCE, config);

    assertThat(sampler)
        .isInstanceOfSatisfying(
            TestConfigurableSamplerProvider.TestSampler.class,
            s -> assertThat(s.getConfig()).isSameAs(config));
  }

  @Test
  void jaegerConfiguration() {
    Resource resource = Resource.create(Attributes.of(stringKey("service.name"), "cat"));
    ConfigProperties config =
        ConfigProperties.createForTest(
            ImmutableMap.of(
                "test.option", "true", "otel.exporter.jaeger.endpoint", "localhost:14259"));

    Sampler sampler = TracerProviderConfiguration.configureSampler("jaeger", resource, config);

    assertThat(sampler).isInstanceOf(JaegerRemoteSampler.class);
  }

  @Test
  void missingServiceNameJaegerConfiguration() {
    ConfigProperties config =
        ConfigProperties.createForTest(
            ImmutableMap.of(
                "test.option", "true", "otel.exporter.jaeger.endpoint", "localhost:14259"));

    assertThatThrownBy(
            () -> TracerProviderConfiguration.configureSampler("jaeger", EMPTY_RESOURCE, config))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("serviceName");
  }

  @Test
  void missingEndpointJaegerConfiguration() {
    Resource resource = Resource.create(Attributes.of(stringKey("service.name"), "cat"));

    ConfigProperties config =
        ConfigProperties.createForTest(ImmutableMap.of("test.option", "true"));

    assertThatThrownBy(
            () -> TracerProviderConfiguration.configureSampler("jaeger", resource, config))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("endpoint");
  }

  @Test
  void samplerNotFound() {
    assertThatThrownBy(
            () ->
                TracerProviderConfiguration.configureSampler(
                    "catSampler",
                    EMPTY_RESOURCE,
                    ConfigProperties.createForTest(Collections.emptyMap())))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("catSampler");
  }
}
