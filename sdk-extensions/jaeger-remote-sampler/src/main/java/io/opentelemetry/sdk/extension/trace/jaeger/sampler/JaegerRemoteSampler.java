/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.export.GrpcResponse;
import io.opentelemetry.sdk.common.export.GrpcSender;
import io.opentelemetry.sdk.common.export.GrpcStatusCode;
import io.opentelemetry.sdk.common.export.MessageWriter;
import io.opentelemetry.sdk.common.internal.DaemonThreadFactory;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/** Remote sampler that gets sampling configuration from remote Jaeger server. */
public final class JaegerRemoteSampler implements Sampler, Closeable {
  private static final Logger logger = Logger.getLogger(JaegerRemoteSampler.class.getName());

  private static final String WORKER_THREAD_NAME =
      JaegerRemoteSampler.class.getSimpleName() + "_WorkerThread";
  private static final String type = "remoteSampling";

  private final String serviceName;
  private final ScheduledExecutorService pollExecutor;
  private final ScheduledFuture<?> pollFuture;

  private volatile Sampler sampler;

  private final GrpcSender grpcSender;

  JaegerRemoteSampler(
      GrpcSender grpcSender,
      @Nullable String serviceName,
      int pollingIntervalMs,
      Sampler initialSampler) {
    this.serviceName = serviceName != null ? serviceName : "";
    this.grpcSender = grpcSender;
    this.sampler = initialSampler;
    pollExecutor = Executors.newScheduledThreadPool(1, new DaemonThreadFactory(WORKER_THREAD_NAME));
    pollFuture =
        pollExecutor.scheduleWithFixedDelay(
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
    SamplingStrategyParametersMarshaler marsher =
        SamplingStrategyParametersMarshaler.create(this.serviceName);
    try {
      MessageWriter messageWriter = marsher.toBinaryMessageWriter();
      grpcSender.send(messageWriter, this::onResponse, JaegerRemoteSampler::onError);
    } catch (Throwable e) { // Catch all to ensure scheduled task continues
      logger.log(Level.WARNING, "Failed to update sampler", e);
    }
  }

  private void onResponse(GrpcResponse grpcResponse) {
    GrpcStatusCode statusCode = grpcResponse.getStatusCode();

    if (statusCode == GrpcStatusCode.OK) {
      try {
        SamplingStrategyResponse strategyResponse =
            SamplingStrategyResponseUnMarshaler.read(grpcResponse.getResponseMessage());
        sampler = updateSampler(strategyResponse);
      } catch (IOException e) {
        logger.log(Level.WARNING, "Failed to unmarshal strategy response", e);
      }
      return;
    }

    switch (statusCode) {
      case UNIMPLEMENTED:
        logger.log(
            Level.SEVERE,
            "Failed to execute "
                + type
                + "s. Server responded with UNIMPLEMENTED. "
                + "Full error message: "
                + grpcResponse.getStatusDescription());
        break;
      case UNAVAILABLE:
        logger.log(
            Level.SEVERE,
            "Failed to execute "
                + type
                + "s. Server is UNAVAILABLE. "
                + "Make sure your service is running and reachable from this network. "
                + "Full error message: "
                + grpcResponse.getStatusDescription());
        break;
      default:
        logger.log(
            Level.WARNING,
            "Failed to execute "
                + type
                + "s. Server responded with gRPC status code "
                + statusCode.name()
                + ". Error message: "
                + grpcResponse.getStatusDescription());
        break;
    }
  }

  private static void onError(Throwable e) {
    logger.log(
        Level.SEVERE,
        "Failed to execute "
            + type
            + "s. The request could not be executed. Error message: "
            + e.getMessage(),
        e);
    if (logger.isLoggable(Level.FINEST)) {
      logger.log(Level.FINEST, "Failed to execute " + type + "s. Details follow: " + e);
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
  @SuppressWarnings("Interruption")
  public void close() {
    pollFuture.cancel(true);
    pollExecutor.shutdownNow();
    grpcSender.shutdown();
  }
}
