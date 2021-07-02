/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import io.opentelemetry.sdk.autoconfigure.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurableSamplerProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JaegerRemoteSamplerProvider implements ConfigurableSamplerProvider {

  private static final String ENDPOINT_KEY = "endpoint";
  private static final String POLLING_INTERVAL = "pollingInterval";
  private static final String INITIAL_SAMPLING_RATE = "initialSamplingRate";

  @Override
  public Sampler createSampler(ConfigProperties config) {
    String serviceName = config.getString("otel.service.name");
    if (serviceName == null) {
      Map<String, String> resourceAttributes =
          config.getCommaSeparatedMap("otel.resource.attributes");
      serviceName = resourceAttributes.get("otel.service.name");
    }

    JaegerRemoteSamplerBuilder builder = JaegerRemoteSampler.builder().setServiceName(serviceName);
    Map<String, String> params = config.getCommaSeparatedMap("otel.traces.sampler.arg");

    // Optional configuration
    String endpoint = params.get(ENDPOINT_KEY);
    if (endpoint != null) {
      builder.setEndpoint(endpoint);
    }
    String pollingInterval = params.get(POLLING_INTERVAL);
    if (pollingInterval != null) {
      builder.setPollingInterval(Integer.valueOf(pollingInterval), TimeUnit.MILLISECONDS);
    }
    String initialSamplingRate = params.get(INITIAL_SAMPLING_RATE);
    if (initialSamplingRate != null) {
      builder.setInitialSampler(Sampler.traceIdRatioBased(Double.valueOf(initialSamplingRate)));
    }
    return builder.build();
  }

  @Override
  public String getName() {
    return "jaeger_remote";
  }
}
