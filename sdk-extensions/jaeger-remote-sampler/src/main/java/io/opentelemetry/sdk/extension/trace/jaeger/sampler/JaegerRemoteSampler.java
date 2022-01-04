/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.internal.DaemonThreadFactory;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.io.Closeable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/** Remote sampler that gets sampling configuration from remote Jaeger server. */
@SuppressWarnings({"SystemOut", "DefaultCharset"})
public final class JaegerRemoteSampler implements Sampler, Closeable {
  private static final Logger logger = Logger.getLogger(JaegerRemoteSampler.class.getName());

  private static final String WORKER_THREAD_NAME =
      JaegerRemoteSampler.class.getSimpleName() + "_WorkerThread";

  private final String serviceName;

  private final ScheduledExecutorService pollExecutor;
  private final ScheduledFuture<?> pollFuture;

  private volatile Sampler sampler;

  private final GrpcService<
          SamplingStrategyParametersMarshaller, SamplingStrategyResponseUnMarshaller>
      delegate;

  JaegerRemoteSampler(
      GrpcService<SamplingStrategyParametersMarshaller, SamplingStrategyResponseUnMarshaller>
          delegate,
      @Nullable String serviceName,
      int pollingIntervalMs,
      Sampler initialSampler) {
    this.serviceName = serviceName != null ? serviceName : "";
    this.delegate = delegate;
    this.sampler = initialSampler;
    pollExecutor = Executors.newScheduledThreadPool(1, new DaemonThreadFactory(WORKER_THREAD_NAME));
    pollFuture =
        pollExecutor.scheduleAtFixedRate(
            this::getAndUpdateSampler, 0, pollingIntervalMs, TimeUnit.MILLISECONDS);
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

  private void getAndUpdateSampler() {
    System.out.println("updating sampler");
    try {
      System.out.println("creating params");
      SamplingStrategyResponseUnMarshaller responseParameters =
          delegate.execute(
              SamplingStrategyParametersMarshaller.create(this.serviceName),
              new SamplingStrategyResponseUnMarshaller());
      @Nullable SamplingStrategyResponse response = responseParameters.get();
      if (response != null) {
        System.out.println(response);
        this.sampler = updateSampler(response);
      }
    } catch (Throwable e) { // keep the timer thread alive
      System.out.println("updating sapler failed");
      System.out.println(e);
      logger.log(Level.WARNING, "Failed to update sampler", e);
    }
  }

  private static Sampler updateSampler(SamplingStrategyResponse response) {
    SamplingStrategyResponse.PerOperationSamplingStrategies operationSampling =
        response.perOperationSamplingStrategies;
    if (operationSampling.strategies.size() > 0) {
      Sampler defaultSampler =
          Sampler.traceIdRatioBased(operationSampling.defaultSamplingProbability);
      return Sampler.parentBased(
          new PerOperationSampler(defaultSampler, operationSampling.strategies));
    }
    switch (response.strategyType) {
      case PROBABILISTIC:
        return Sampler.parentBased(
            Sampler.traceIdRatioBased(response.probabilisticSamplingStrategy.samplingRate));
      case RATE_LIMITING:
        return Sampler.parentBased(
            new RateLimitingSampler(response.rateLimitingSamplingStrategy.maxTracesPerSecond));
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

  @Override
  public void close() {
    pollFuture.cancel(true);
    pollExecutor.shutdown();
    delegate.shutdown();
  }
}
