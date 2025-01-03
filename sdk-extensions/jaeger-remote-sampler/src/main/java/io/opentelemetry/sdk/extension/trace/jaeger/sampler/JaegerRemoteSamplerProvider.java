/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSamplerProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JaegerRemoteSamplerProvider implements ConfigurableSamplerProvider {

  private static final Logger LOGGER =
      Logger.getLogger(JaegerRemoteSamplerProvider.class.getName());

  // visible for testing
  static final String ATTRIBUTE_PROPERTY = "otel.resource.attributes";
  static final String SERVICE_NAME_PROPERTY = "otel.service.name";
  static final String SAMPLER_ARG_PROPERTY = "otel.traces.sampler.arg";
  static final String RESOURCE_ATTRIBUTE_SERVICE_NAME_PROPERTY = "service.name";
  private static final String ENDPOINT_KEY = "endpoint";
  private static final String POLLING_INTERVAL = "pollingIntervalMs";
  private static final String INITIAL_SAMPLING_RATE = "initialSamplingRate";

  @Override
  public Sampler createSampler(ConfigProperties config) {
    JaegerRemoteSamplerBuilder builder = JaegerRemoteSampler.builder();

    String serviceName = config.getString(SERVICE_NAME_PROPERTY);
    if (serviceName == null) {
      Map<String, String> resourceAttributes = config.getMap(ATTRIBUTE_PROPERTY);
      serviceName = resourceAttributes.get(RESOURCE_ATTRIBUTE_SERVICE_NAME_PROPERTY);
    }
    if (serviceName != null) {
      builder.setServiceName(serviceName);
    }

    Map<String, String> params = config.getMap(SAMPLER_ARG_PROPERTY);

    // Optional configuration
    String endpoint = params.get(ENDPOINT_KEY);
    if (endpoint != null) {
      builder.setEndpoint(endpoint);
    }
    String pollingInterval = params.get(POLLING_INTERVAL);
    // Previously, we mistakenly read from pollingInterval. For backwards compatibility, check
    // pollingInterval and log warning if set.
    if (pollingInterval == null) {
      pollingInterval = params.get("pollingInterval");
      if (pollingInterval != null) {
        LOGGER.log(
            Level.WARNING,
            SAMPLER_ARG_PROPERTY
                + " contains deprecated \"pollingInterval\" property. Please use \""
                + POLLING_INTERVAL
                + "\" instead.");
      }
    }
    if (pollingInterval != null) {
      builder.setPollingInterval(Integer.valueOf(pollingInterval), TimeUnit.MILLISECONDS);
    }

    String initialSamplingRate = params.get(INITIAL_SAMPLING_RATE);
    if (initialSamplingRate != null) {
      builder.setInitialSampler(
          Sampler.parentBased(Sampler.traceIdRatioBased(Double.valueOf(initialSamplingRate))));
    }
    return builder.build();
  }

  @Override
  public String getName() {
    return "jaeger_remote";
  }
}
