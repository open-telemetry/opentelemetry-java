/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import io.grpc.ManagedChannel;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.Sampling.PerOperationSamplingStrategies;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.Sampling.SamplingStrategyParameters;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.Sampling.SamplingStrategyResponse;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.SamplingManagerGrpc;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.SamplingManagerGrpc.SamplingManagerBlockingStub;
import io.opentelemetry.sdk.internal.DaemonThreadFactory;
import io.opentelemetry.sdk.trace.data.SpanData.Link;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Remote sampler that gets sampling configuration from remote Jaeger server. */
public final class JaegerRemoteSampler implements Sampler {
  private static final Logger logger = Logger.getLogger(JaegerRemoteSampler.class.getName());

  private static final String WORKER_THREAD_NAME =
      JaegerRemoteSampler.class.getSimpleName() + "_WorkerThread";

  private final String serviceName;
  private final SamplingManagerBlockingStub stub;

  private volatile Sampler sampler;

  @SuppressWarnings("FutureReturnValueIgnored")
  JaegerRemoteSampler(
      String serviceName, ManagedChannel channel, int pollingIntervalMs, Sampler initialSampler) {
    this.serviceName = serviceName;
    this.stub = SamplingManagerGrpc.newBlockingStub(channel);
    this.sampler = initialSampler;
    ScheduledExecutorService scheduledExecutorService =
        Executors.newScheduledThreadPool(1, new DaemonThreadFactory(WORKER_THREAD_NAME));
    scheduledExecutorService.scheduleAtFixedRate(
        this::getAndUpdateSampler, 0, pollingIntervalMs, TimeUnit.MILLISECONDS);
  }

  @Override
  public SamplingResult shouldSample(
      Context parentContext,
      String traceId,
      String name,
      Kind spanKind,
      Attributes attributes,
      List<Link> parentLinks) {
    return sampler.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
  }

  private void getAndUpdateSampler() {
    try {
      SamplingStrategyParameters params =
          SamplingStrategyParameters.newBuilder().setServiceName(this.serviceName).build();
      SamplingStrategyResponse response = stub.getSamplingStrategy(params);
      this.sampler = updateSampler(response);
    } catch (Exception e) { // keep the timer thread alive
      logger.log(Level.WARNING, "Failed to update sampler", e);
    }
  }

  private static Sampler updateSampler(SamplingStrategyResponse response) {
    PerOperationSamplingStrategies operationSampling = response.getOperationSampling();
    if (operationSampling.getPerOperationStrategiesList().size() > 0) {
      Sampler defaultSampler =
          Sampler.traceIdRatioBased(operationSampling.getDefaultSamplingProbability());
      return Sampler.parentBased(
          new PerOperationSampler(
              defaultSampler, operationSampling.getPerOperationStrategiesList()));
    }
    switch (response.getStrategyType()) {
      case PROBABILISTIC:
        return Sampler.parentBased(
            Sampler.traceIdRatioBased(response.getProbabilisticSampling().getSamplingRate()));
      case RATE_LIMITING:
        return Sampler.parentBased(
            new RateLimitingSampler(response.getRateLimitingSampling().getMaxTracesPerSecond()));
      case UNRECOGNIZED:
        throw new AssertionError("unrecognized sampler type");
    }
    throw new AssertionError("unrecognized sampler type");
  }

  @Override
  public String getDescription() {
    return String.format("JaegerRemoteSampler{%s}", this.sampler);
  }

  @Override
  public String toString() {
    return getDescription();
  }

  // Visible for testing
  Sampler getSampler() {
    return this.sampler;
  }

  public static JaegerRemoteSamplerBuilder builder() {
    return new JaegerRemoteSamplerBuilder();
  }
}
