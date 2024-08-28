/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler.internal;

import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.extension.incubator.fileconfig.FileConfiguration;
import io.opentelemetry.sdk.extension.trace.jaeger.sampler.JaegerRemoteSampler;
import io.opentelemetry.sdk.extension.trace.jaeger.sampler.JaegerRemoteSamplerBuilder;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.time.Duration;

/**
 * File configuration SPI implementation for {@link JaegerRemoteSampler}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class JaegerRemoteSamplerComponentProvider implements ComponentProvider<Sampler> {
  @Override
  public Class<Sampler> getType() {
    return Sampler.class;
  }

  @Override
  public String getName() {
    return "jaeger_remote";
  }

  @Override
  public Sampler create(StructuredConfigProperties config) {
    JaegerRemoteSamplerBuilder builder = JaegerRemoteSampler.builder();

    // Optional configuration
    String endpoint = config.getString("endpoint");
    if (endpoint != null) {
      builder.setEndpoint(endpoint);
    }
    Long pollingIntervalMs = config.getLong("internal");
    if (pollingIntervalMs != null) {
      builder.setPollingInterval(Duration.ofMillis(pollingIntervalMs));
    }
    StructuredConfigProperties initialSamplerModel = config.getStructured("initial_sampler");
    if (initialSamplerModel != null) {
      Sampler initialSampler = FileConfiguration.createSampler(initialSamplerModel);
      builder.setInitialSampler(initialSampler);
    }

    return builder.build();
  }
}
