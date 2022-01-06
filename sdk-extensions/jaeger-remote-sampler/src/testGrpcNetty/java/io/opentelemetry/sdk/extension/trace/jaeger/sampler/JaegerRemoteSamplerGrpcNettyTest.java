/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import com.google.common.io.Closer;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.grpc.protocol.AbstractUnaryGrpcService;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.Sampling;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.Sampling.RateLimitingSamplingStrategy;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.Sampling.SamplingStrategyType;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.awaitility.core.ThrowingRunnable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class JaegerRemoteSamplerGrpcNettyTest {

  private static final String SERVICE_NAME = "my-service";
  private static final int RATE = 999;

  private static final AtomicInteger numPolls = new AtomicInteger();

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
                  Sampling.SamplingStrategyResponse response =
                      Sampling.SamplingStrategyResponse.newBuilder()
                          .setStrategyType(SamplingStrategyType.RATE_LIMITING)
                          .setRateLimitingSampling(
                              RateLimitingSamplingStrategy.newBuilder()
                                  .setMaxTracesPerSecond(RATE)
                                  .build())
                          .build();
                  numPolls.incrementAndGet();
                  return CompletableFuture.completedFuture(response.toByteArray());
                }
              });
        }
      };

  private final Closer closer = Closer.create();

  @BeforeEach
  public void before() {
    numPolls.set(0);
  }

  @AfterEach
  void tearDown() throws Exception {
    closer.close();
  }

  @Test
  void connectionWorks() {
    JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setEndpoint(server.httpUri().toString())
            .setPollingInterval(1, TimeUnit.SECONDS)
            .setServiceName(SERVICE_NAME)
            .build();
    closer.register(sampler);

    await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(samplerIsType(sampler, RateLimitingSampler.class));

    // verify
    assertThat(sampler.getDescription()).contains("RateLimitingSampler{999.00}");
    assertThat(numPolls).hasValueGreaterThanOrEqualTo(1);
  }

  @Test
  void description() {
    JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setEndpoint(server.httpUri().toString())
            .setPollingInterval(1, TimeUnit.SECONDS)
            .setServiceName(SERVICE_NAME)
            .build();
    closer.register(sampler);
    assertThat(sampler.getDescription())
        .startsWith("JaegerRemoteSampler{ParentBased{root:TraceIdRatioBased{0.001000}");

    // wait until the sampling strategy is retrieved before exiting test method
    await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(samplerIsType(sampler, RateLimitingSampler.class));

    assertThat(numPolls).hasValueGreaterThanOrEqualTo(1);
  }

  @Test
  void initialSampler() {
    JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setEndpoint("http://example.com")
            .setServiceName(SERVICE_NAME)
            .setInitialSampler(Sampler.alwaysOn())
            .build();
    closer.register(sampler);
    assertThat(sampler.getDescription()).startsWith("JaegerRemoteSampler{AlwaysOnSampler}");
  }

  @Test
  void pollingInterval() throws Exception {
    JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setEndpoint(server.httpUri().toString())
            .setServiceName(SERVICE_NAME)
            .setPollingInterval(1, TimeUnit.MILLISECONDS)
            .build();
    closer.register(sampler);

    // wait until the sampling strategy is retrieved before exiting test method
    await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(samplerIsType(sampler, RateLimitingSampler.class));

    Thread.sleep(500);

    assertThat(numPolls).hasValueGreaterThanOrEqualTo(2);
  }

  @Test
  void pollingInterval_duration() throws Exception {
    JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setEndpoint(server.httpUri().toString())
            .setServiceName(SERVICE_NAME)
            .setPollingInterval(Duration.ofMillis(1))
            .build();
    closer.register(sampler);

    // wait until the sampling strategy is retrieved before exiting test method
    await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(samplerIsType(sampler, RateLimitingSampler.class));

    Thread.sleep(500);

    assertThat(numPolls).hasValueGreaterThanOrEqualTo(2);
  }

  @Test
  void invalidArguments() {
    assertThatThrownBy(() -> JaegerRemoteSampler.builder().setServiceName(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("serviceName");
    assertThatThrownBy(() -> JaegerRemoteSampler.builder().setEndpoint(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("endpoint");
    assertThatThrownBy(
            () -> JaegerRemoteSampler.builder().setPollingInterval(-1, TimeUnit.MILLISECONDS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("polling interval must be positive");
    assertThatThrownBy(() -> JaegerRemoteSampler.builder().setPollingInterval(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
    assertThatThrownBy(
            () -> JaegerRemoteSampler.builder().setPollingInterval(Duration.ofMillis(-1)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("polling interval must be positive");
    assertThatThrownBy(() -> JaegerRemoteSampler.builder().setPollingInterval(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("interval");
  }

  @Test
  void usingGrpc() {
    assertThat(JaegerRemoteSampler.builder().getDelegate())
        .isInstanceOf(DefaultGrpcServiceBuilder.class);
  }

  static ThrowingRunnable samplerIsType(
      final JaegerRemoteSampler sampler, final Class<? extends Sampler> expected) {
    return () -> {
      assertThat(sampler.getSampler().getClass().getName())
          .isEqualTo("io.opentelemetry.sdk.trace.samplers.ParentBasedSampler");

      Field field = sampler.getSampler().getClass().getDeclaredField("root");
      field.setAccessible(true);
      assertThat(field.get(sampler.getSampler()).getClass()).isEqualTo(expected);
    };
  }
}
