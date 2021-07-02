/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigurableSamplerProvider;
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
  }
}
