/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.trace;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.OtelEncodingUtils;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.internal.DaemonThreadFactory;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.io.Closeable;
import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/** Remote sampler that gets sampling configuration from AWS X-Ray. */
public final class AwsXrayRemoteSampler implements Sampler, Closeable {

  private static final Logger logger = Logger.getLogger(AwsXrayRemoteSampler.class.getName());

  private static final String WORKER_THREAD_NAME =
      AwsXrayRemoteSampler.class.getSimpleName() + "_WorkerThread";

  // Unique per-process client ID, generated as a random string.
  private static final String CLIENT_ID = generateClientId();

  private final Resource resource;
  private final Sampler initialSampler;
  private final XraySamplerClient client;
  private final ScheduledExecutorService executor;
  private final ScheduledFuture<?> pollFuture;

  @Nullable private volatile GetSamplingRulesResponse previousRulesResponse;
  private volatile Sampler sampler;

  /**
   * Returns a {@link AwsXrayRemoteSamplerBuilder} with the given {@link Resource}. This {@link
   * Resource} should be the same as what the {@linkplain io.opentelemetry.sdk.OpenTelemetrySdk
   * OpenTelemetry SDK} is configured with.
   */
  // TODO(anuraaga): Deprecate after
  // https://github.com/open-telemetry/opentelemetry-specification/issues/1588
  public static AwsXrayRemoteSamplerBuilder newBuilder(Resource resource) {
    return new AwsXrayRemoteSamplerBuilder(resource);
  }

  AwsXrayRemoteSampler(
      Resource resource, String endpoint, Sampler initialSampler, long pollingIntervalNanos) {
    this.resource = resource;
    this.initialSampler = initialSampler;
    client = new XraySamplerClient(endpoint);
    executor =
        Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory(WORKER_THREAD_NAME));

    sampler = initialSampler;

    pollFuture =
        executor.scheduleAtFixedRate(
            this::getAndUpdateSampler, 0, pollingIntervalNanos, TimeUnit.NANOSECONDS);
  }

  @Override
  public SamplingResult shouldSample(
      Context parentContext,
      String traceId,
      String name,
      SpanKind spanKind,
      Attributes attributes,
      List<LinkData> parentLinks) {
    return sampler.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
  }

  @Override
  public String getDescription() {
    return "AwsXrayRemoteSampler{" + sampler.getDescription() + "}";
  }

  private void getAndUpdateSampler() {
    try {
      // No pagination support yet, or possibly ever.
      GetSamplingRulesResponse response =
          client.getSamplingRules(GetSamplingRulesRequest.create(null));
      if (!response.equals(previousRulesResponse)) {
        sampler =
            new XrayRulesSampler(CLIENT_ID, resource, initialSampler, response.getSamplingRules());
        previousRulesResponse = response;
      }
    } catch (Throwable t) {
      logger.log(Level.FINE, "Failed to update sampler", t);
    }
  }

  @Override
  public void close() {
    pollFuture.cancel(true);
    executor.shutdownNow();
    // No flushing behavior so no need to wait for the shutdown.
  }

  private static String generateClientId() {
    SecureRandom rand = new SecureRandom();
    byte[] bytes = new byte[12];
    rand.nextBytes(bytes);
    char[] clientIdChars = new char[24];
    OtelEncodingUtils.bytesToBase16(bytes, clientIdChars, 12);
    return new String(clientIdChars);
  }
}
