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
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurableSamplerProvider;
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
    samplerArgs.put("endpoint", "localhost:9999");
    samplerArgs.put("pollingInterval", "99");
    double samplingRate = 0.33;
    samplerArgs.put("initialSamplingRate", String.valueOf(samplingRate));
    when(mockConfig.getCommaSeparatedMap(JaegerRemoteSamplerProvider.SAMPLER_ARG_PROPERTY))
        .thenReturn(samplerArgs);

    Sampler sampler = Sampler.parentBased(Sampler.traceIdRatioBased(samplingRate));
    assertThat(samplerProviders)
        .singleElement(type(JaegerRemoteSamplerProvider.class))
        .satisfies(
            provider ->
                assertThat(provider.createSampler(mockConfig))
                    .extracting("sampler", type(Sampler.class))
                    .asString()
                    .isEqualTo(sampler.toString()))
        .satisfies(
            provider ->
                assertThat(provider.createSampler(mockConfig))
                    .extracting("serviceName")
                    .isEqualTo("test_service"))
        .satisfies(
            provider ->
                assertThat(provider.createSampler(mockConfig))
                    .extracting("channel")
                    .extracting("delegate")
                    .extracting("target")
                    .isEqualTo("localhost:9999"));
  }

  @Test
  void serviceNameInAttributeProperties() {
    ConfigProperties mockConfig = mock(ConfigProperties.class);
    HashMap<String, String> attributeProperties = new HashMap<>();
    attributeProperties.put(JaegerRemoteSamplerProvider.SERVICE_NAME_PROPERTY, "test_service2");
    when(mockConfig.getCommaSeparatedMap(JaegerRemoteSamplerProvider.ATTRIBUTE_PROPERTY))
        .thenReturn(attributeProperties);
    ServiceLoader<ConfigurableSamplerProvider> samplerProviders =
        ServiceLoader.load(ConfigurableSamplerProvider.class);
    assertThat(samplerProviders)
        .singleElement(type(JaegerRemoteSamplerProvider.class))
        .satisfies(
            provider ->
                assertThat(provider.createSampler(mockConfig))
                    .extracting("serviceName")
                    .isEqualTo("test_service2"));
  }
}
