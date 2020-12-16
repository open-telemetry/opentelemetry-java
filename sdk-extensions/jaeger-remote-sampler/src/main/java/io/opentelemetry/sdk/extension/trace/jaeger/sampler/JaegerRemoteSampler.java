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
public class JaegerRemoteSampler implements Sampler {
  private static final Logger logger = Logger.getLogger(JaegerRemoteSampler.class.getName());

  private static final String WORKER_THREAD_NAME =
      JaegerRemoteSampler.class.getSimpleName() + "_WorkerThread";
  private static final int DEFAULT_POLLING_INTERVAL_MS = 60000;
  private static final Sampler INITIAL_SAMPLER = Sampler.traceIdRatioBased(0.001);

  private final String serviceName;
  private final SamplingManagerBlockingStub stub;
  private Sampler sampler;

  @SuppressWarnings("FutureReturnValueIgnored")
  private JaegerRemoteSampler(
      String serviceName, ManagedChannel channel, int pollingIntervalMs, Sampler initialSampler) {
    this.serviceName = serviceName;
    this.stub = SamplingManagerGrpc.newBlockingStub(channel);
    this.sampler = initialSampler;
    ScheduledExecutorService scheduledExecutorService =
        Executors.newScheduledThreadPool(1, new DaemonThreadFactory(WORKER_THREAD_NAME));
    scheduledExecutorService.scheduleAtFixedRate(
        updateSampleRunnable(), 0, pollingIntervalMs, TimeUnit.MILLISECONDS);
  }

  private Runnable updateSampleRunnable() {
    return new Runnable() {
      @Override
      public void run() {
        try {
          getAndUpdateSampler();
        } catch (Exception e) { // keep the timer thread alive
          logger.log(Level.WARNING, "Failed to update sampler", e);
        }
      }
    };
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
    SamplingStrategyParameters params =
        SamplingStrategyParameters.newBuilder().setServiceName(this.serviceName).build();
    SamplingStrategyResponse response = stub.getSamplingStrategy(params);
    this.sampler = updateSampler(response);
  }

  private static Sampler updateSampler(SamplingStrategyResponse response) {
    PerOperationSamplingStrategies operationSampling = response.getOperationSampling();
    if (operationSampling != null && operationSampling.getPerOperationStrategiesList().size() > 0) {
      Sampler defaultSampler =
          Sampler.traceIdRatioBased(operationSampling.getDefaultSamplingProbability());
      return new PerOperationSampler(
          defaultSampler, operationSampling.getPerOperationStrategiesList());
    }
    switch (response.getStrategyType()) {
      case PROBABILISTIC:
        return Sampler.traceIdRatioBased(response.getProbabilisticSampling().getSamplingRate());
      case RATE_LIMITING:
        return new RateLimitingSampler(response.getRateLimitingSampling().getMaxTracesPerSecond());
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

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private ManagedChannel channel;
    private String serviceName;
    private Sampler initialSampler = INITIAL_SAMPLER;
    private int pollingIntervalMs = DEFAULT_POLLING_INTERVAL_MS;

    /**
     * Sets the service name to be used by this exporter. Required.
     *
     * @param serviceName the service name.
     * @return this.
     */
    public Builder setServiceName(String serviceName) {
      this.serviceName = serviceName;
      return this;
    }

    /**
     * Sets the managed chanel to use when communicating with the backend. Required.
     *
     * @param channel the channel to use.
     * @return this.
     */
    public Builder setChannel(ManagedChannel channel) {
      this.channel = channel;
      return this;
    }

    /**
     * Sets the polling interval. Optional.
     *
     * @param pollingIntervalMs the polling interval in Ms.
     * @return this.
     */
    public Builder withPollingInterval(int pollingIntervalMs) {
      this.pollingIntervalMs = pollingIntervalMs;
      return this;
    }

    /**
     * Sets the initial sampler that is used before sampling configuration is obtained from the
     * server. By default probabilistic sampler with is used with probability 0.001. Optional.
     *
     * @param initialSampler the initial sampler to use.
     * @return this.
     */
    public Builder withInitialSampler(Sampler initialSampler) {
      this.initialSampler = initialSampler;
      return this;
    }

    /**
     * Builds the {@link JaegerRemoteSampler}.
     *
     * @return the remote sampler instance.
     */
    public JaegerRemoteSampler build() {
      return new JaegerRemoteSampler(
          this.serviceName, this.channel, this.pollingIntervalMs, this.initialSampler);
    }

    private Builder() {}
  }
}
