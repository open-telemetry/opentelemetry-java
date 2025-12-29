/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler.internal;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.DeclarativeConfiguration;
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
public class JaegerRemoteSamplerComponentProvider implements ComponentProvider {
  @Override
  public Class<Sampler> getType() {
    return Sampler.class;
  }

  @Override
  public String getName() {
    return "jaeger_remote/development";
  }

  @Override
  public Sampler create(DeclarativeConfigProperties config) {
    JaegerRemoteSamplerBuilder builder = JaegerRemoteSampler.builder();

    String endpoint = config.getString("endpoint");
    if (endpoint == null) {
      throw new DeclarativeConfigException("jaeger remote sampler endpoint is required");
    }
    builder.setEndpoint(endpoint);

    DeclarativeConfigProperties initialSamplerModel = config.getStructured("initial_sampler");
    if (initialSamplerModel == null) {
      throw new DeclarativeConfigException("jaeger remote sampler initial_sampler is required");
    }
    builder.setInitialSampler(DeclarativeConfiguration.createSampler(initialSamplerModel));

    Long pollingIntervalMs = config.getLong("internal");
    if (pollingIntervalMs != null) {
      builder.setPollingInterval(Duration.ofMillis(pollingIntervalMs));
    }

    return builder.build();
  }
}
