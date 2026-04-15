/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSamplerProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.HashMap;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;

public class JaegerRemoteSamplerProviderTest {

  @Test
  void serviceProvider() {
    ServiceLoader<ConfigurableSamplerProvider> samplerProviders =
        ServiceLoader.load(ConfigurableSamplerProvider.class);
    assertThat(samplerProviders).hasAtLeastOneElementOfType(JaegerRemoteSamplerProvider.class);
    assertThat(samplerProviders)
        .singleElement(type(JaegerRemoteSamplerProvider.class))
        .satisfies(provider -> assertThat(provider.getName()).isEqualTo("jaeger_remote"));

    ConfigProperties mockConfig = mock(ConfigProperties.class);
    when(mockConfig.getString(JaegerRemoteSamplerProvider.SERVICE_NAME_PROPERTY))
        .thenReturn("test_service");
    HashMap<String, String> samplerArgs = new HashMap<>();
    samplerArgs.put("endpoint", "http://localhost:9999");
    samplerArgs.put("pollingIntervalMs", "99");
    double samplingRate = 0.33;
    samplerArgs.put("initialSamplingRate", String.valueOf(samplingRate));
    when(mockConfig.getMap(JaegerRemoteSamplerProvider.SAMPLER_ARG_PROPERTY))
        .thenReturn(samplerArgs);

    Sampler sampler = Sampler.parentBased(Sampler.traceIdRatioBased(samplingRate));
    assertThat(samplerProviders)
        .singleElement(type(JaegerRemoteSamplerProvider.class))
        .satisfies(
            provider -> {
              try (JaegerRemoteSampler s =
                  (JaegerRemoteSampler) provider.createSampler(mockConfig)) {
                assertThat(s)
                    .extracting("sampler", type(Sampler.class))
                    .asString()
                    .isEqualTo(sampler.toString());
                assertThat(s).extracting("serviceName").isEqualTo("test_service");
                assertThat(s)
                    .extracting("grpcSender")
                    .extracting("url")
                    .satisfies(
                        url ->
                            assertThat(url.toString())
                                .isEqualTo(
                                    "http://localhost:9999/jaeger.api_v2.SamplingManager/GetSamplingStrategy"));
              }
            });
  }

  @Test
  void serviceNameInAttributeProperties() {
    ConfigProperties mockConfig = mock(ConfigProperties.class);
    HashMap<String, String> attributeProperties = new HashMap<>();
    attributeProperties.put(
        JaegerRemoteSamplerProvider.RESOURCE_ATTRIBUTE_SERVICE_NAME_PROPERTY, "test_service2");
    when(mockConfig.getMap(JaegerRemoteSamplerProvider.ATTRIBUTE_PROPERTY))
        .thenReturn(attributeProperties);
    ServiceLoader<ConfigurableSamplerProvider> samplerProviders =
        ServiceLoader.load(ConfigurableSamplerProvider.class);
    assertThat(samplerProviders)
        .singleElement(type(JaegerRemoteSamplerProvider.class))
        .satisfies(
            provider -> {
              try (JaegerRemoteSampler s =
                  (JaegerRemoteSampler) provider.createSampler(mockConfig)) {
                assertThat(s).extracting("serviceName").isEqualTo("test_service2");
              }
            });
  }
}
