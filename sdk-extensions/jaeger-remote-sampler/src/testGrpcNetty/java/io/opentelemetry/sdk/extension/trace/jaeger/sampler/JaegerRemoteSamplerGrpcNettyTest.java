/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.linecorp.armeria.common.grpc.protocol.ArmeriaStatusException;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.grpc.protocol.AbstractUnaryGrpcService;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.github.netmikey.logunit.api.LogCapturer;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.Sampling;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.Sampling.RateLimitingSamplingStrategy;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.Sampling.SamplingStrategyType;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.awaitility.core.ThrowingRunnable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

@SuppressLogger(UpstreamGrpcService.class)
@SuppressWarnings("deprecation") // Testing deprecated code
class JaegerRemoteSamplerGrpcNettyTest {

  private static final String SERVICE_NAME = "my-service";
  private static final int RATE = 999;

  private static final ConcurrentLinkedQueue<ArmeriaStatusException> grpcErrors =
      new ConcurrentLinkedQueue<>();

  private static final ConcurrentLinkedQueue<Sampling.SamplingStrategyResponse> responses =
      new ConcurrentLinkedQueue<>();

  private static void addGrpcError(int code, @Nullable String message) {
    grpcErrors.add(new ArmeriaStatusException(code, message));
  }

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(UpstreamGrpcService.class, Level.TRACE);

  @Order(1)
  @RegisterExtension
  static final SelfSignedCertificateExtension certificate = new SelfSignedCertificateExtension();

  @Order(2)
  @RegisterExtension
  static final ServerExtension server =
      new ServerExtension() {
        @Override
        protected void configure(ServerBuilder sb) {
          sb.service(
              JaegerRemoteSamplerBuilder.GRPC_ENDPOINT_PATH,
              new AbstractUnaryGrpcService() {
                @Override
                protected CompletionStage<byte[]> handleMessage(
                    ServiceRequestContext ctx, byte[] message) {

                  ArmeriaStatusException grpcError = grpcErrors.poll();
                  if (grpcError != null) {
                    throw grpcError;
                  }

                  Sampling.SamplingStrategyResponse response = responses.poll();
                  // use default
                  if (response == null) {
                    response =
                        Sampling.SamplingStrategyResponse.newBuilder()
                            .setStrategyType(SamplingStrategyType.RATE_LIMITING)
                            .setRateLimitingSampling(
                                RateLimitingSamplingStrategy.newBuilder()
                                    .setMaxTracesPerSecond(RATE)
                                    .build())
                            .build();
                  }

                  return CompletableFuture.completedFuture(response.toByteArray());
                }
              });
          sb.http(0);
        }
      };

  @BeforeEach
  public void before() {
    grpcErrors.clear();
    responses.clear();
  }

  private static ManagedChannel managedChannel() {
    return ManagedChannelBuilder.forTarget(server.httpUri().getAuthority()).usePlaintext().build();
  }

  @Test
  void connectionWorks() {
    try (JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setChannel(managedChannel())
            .setEndpoint(server.httpUri().toString())
            .setPollingInterval(1, TimeUnit.SECONDS)
            .setServiceName(SERVICE_NAME)
            .build()) {
      assertThat(sampler).extracting("delegate").isInstanceOf(UpstreamGrpcService.class);

      await().untilAsserted(samplerIsType(sampler, RateLimitingSampler.class));

      // verify
      assertThat(sampler.getDescription()).contains("RateLimitingSampler{999.00}");
    }
  }

  @Test
  void description() {
    try (JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setChannel(managedChannel())
            .setPollingInterval(1, TimeUnit.SECONDS)
            .setServiceName(SERVICE_NAME)
            .build()) {
      assertThat(sampler).extracting("delegate").isInstanceOf(UpstreamGrpcService.class);

      assertThat(sampler.getDescription())
          .startsWith("JaegerRemoteSampler{ParentBased{root:TraceIdRatioBased{0.001000}");

      // wait until the sampling strategy is retrieved before exiting test method
      await().untilAsserted(samplerIsType(sampler, RateLimitingSampler.class));
    }
  }

  @Test
  void initialSampler() {
    try (JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setChannel(managedChannel())
            .setServiceName(SERVICE_NAME)
            .setInitialSampler(Sampler.alwaysOn())
            .build()) {
      assertThat(sampler).extracting("delegate").isInstanceOf(UpstreamGrpcService.class);

      assertThat(sampler.getDescription()).startsWith("JaegerRemoteSampler{AlwaysOnSampler}");
    }
  }

  @Test
  void pollingInterval() {
    try (JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setChannel(managedChannel())
            .setServiceName(SERVICE_NAME)
            .setPollingInterval(1, TimeUnit.MILLISECONDS)
            .build()) {
      assertThat(sampler).extracting("delegate").isInstanceOf(UpstreamGrpcService.class);

      // wait until the sampling strategy is retrieved before exiting test method
      await().untilAsserted(samplerIsType(sampler, RateLimitingSampler.class));
    }
  }

  @Test
  void pollingInterval_duration() {
    try (JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setChannel(managedChannel())
            .setServiceName(SERVICE_NAME)
            .setPollingInterval(Duration.ofMillis(1))
            .build()) {
      assertThat(sampler).extracting("delegate").isInstanceOf(UpstreamGrpcService.class);

      // wait until the sampling strategy is retrieved before exiting test method
      await().untilAsserted(samplerIsType(sampler, RateLimitingSampler.class));
    }
  }

  @Test
  void perOperationSampling() {
    Sampling.SamplingStrategyResponse response =
        Sampling.SamplingStrategyResponse.newBuilder()
            .setOperationSampling(
                Sampling.PerOperationSamplingStrategies.newBuilder()
                    .setDefaultSamplingProbability(0.55)
                    .setDefaultLowerBoundTracesPerSecond(100)
                    .setDefaultUpperBoundTracesPerSecond(200)
                    .addPerOperationStrategies(
                        Sampling.OperationSamplingStrategy.newBuilder()
                            .setOperation("foo")
                            .setProbabilisticSampling(
                                Sampling.ProbabilisticSamplingStrategy.newBuilder()
                                    .setSamplingRate(0.90)
                                    .build())
                            .build())
                    .addPerOperationStrategies(
                        Sampling.OperationSamplingStrategy.newBuilder()
                            .setOperation("bar")
                            .setProbabilisticSampling(
                                Sampling.ProbabilisticSamplingStrategy.newBuilder()
                                    .setSamplingRate(0.7)
                                    .build())
                            .build())
                    .build())
            .setRateLimitingSampling(
                RateLimitingSamplingStrategy.newBuilder().setMaxTracesPerSecond(RATE).build())
            .build();
    responses.add(response);

    try (JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setChannel(managedChannel())
            .setServiceName(SERVICE_NAME)
            // Make sure only polls once.
            .setPollingInterval(500, TimeUnit.SECONDS)
            .build()) {
      assertThat(sampler).extracting("delegate").isInstanceOf(UpstreamGrpcService.class);

      await()
          .untilAsserted(
              () -> {
                assertThat(sampler.getDescription())
                    .startsWith(
                        "JaegerRemoteSampler{ParentBased{root:PerOperationSampler{default=TraceIdRatioBased{0.550000}, perOperation={foo=TraceIdRatioBased{0.900000}, bar=TraceIdRatioBased{0.700000}}}");
                assertThat(sampler.getDescription()).contains("bar");
              });
    }
  }

  @Test
  void internal_error_server_response() {
    addGrpcError(13, "internal error");

    try (JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setChannel(managedChannel())
            .setServiceName(SERVICE_NAME)
            .setPollingInterval(50, TimeUnit.MILLISECONDS)
            .build()) {
      assertThat(sampler).extracting("delegate").isInstanceOf(UpstreamGrpcService.class);

      assertThat(sampler.getDescription())
          .startsWith("JaegerRemoteSampler{ParentBased{root:TraceIdRatioBased{0.001000}");

      await()
          .untilAsserted(
              () -> {
                LoggingEvent log =
                    logs.assertContains(" Server responded with gRPC status code 13");
                assertThat(log.getLevel()).isEqualTo(Level.WARN);
              });
    }
  }

  @Test
  void unavailable_error_server_response() {
    addGrpcError(14, "クマ🐻 resource exhausted");

    try (JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setChannel(managedChannel())
            .setServiceName(SERVICE_NAME)
            .setPollingInterval(50, TimeUnit.MILLISECONDS)
            .build()) {
      assertThat(sampler).extracting("delegate").isInstanceOf(UpstreamGrpcService.class);

      assertThat(sampler.getDescription())
          .startsWith("JaegerRemoteSampler{ParentBased{root:TraceIdRatioBased{0.001000}");

      await()
          .untilAsserted(
              () -> {
                LoggingEvent log = logs.assertContains("Server is UNAVAILABLE");
                assertThat(log.getLevel()).isEqualTo(Level.ERROR);
              });
    }
  }

  @Test
  void unimplemented_error_server_response() {
    addGrpcError(12, null);

    try (JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setChannel(managedChannel())
            .setServiceName(SERVICE_NAME)
            .setPollingInterval(50, TimeUnit.MILLISECONDS)
            .build()) {
      assertThat(sampler).extracting("delegate").isInstanceOf(UpstreamGrpcService.class);

      assertThat(sampler.getDescription())
          .startsWith("JaegerRemoteSampler{ParentBased{root:TraceIdRatioBased{0.001000}");

      await()
          .untilAsserted(
              () -> {
                LoggingEvent log = logs.assertContains("Server responded with UNIMPLEMENTED.");
                assertThat(log.getLevel()).isEqualTo(Level.ERROR);
              });
    }
  }

  static ThrowingRunnable samplerIsType(
      JaegerRemoteSampler sampler, Class<? extends Sampler> expected) {
    return () -> {
      assertThat(sampler.getSampler().getClass().getName())
          .isEqualTo("io.opentelemetry.sdk.trace.samplers.ParentBasedSampler");
      assertThat(sampler.getSampler()).extracting("root").isInstanceOf(expected);
    };
  }
}
