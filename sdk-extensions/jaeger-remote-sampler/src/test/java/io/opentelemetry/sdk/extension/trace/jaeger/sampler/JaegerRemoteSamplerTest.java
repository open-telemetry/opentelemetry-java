/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.io.Closer;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.Sampling;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.Sampling.RateLimitingSamplingStrategy;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.Sampling.SamplingStrategyParameters;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.Sampling.SamplingStrategyType;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.SamplingManagerGrpc;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.awaitility.core.ThrowingRunnable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

class JaegerRemoteSamplerTest {

  private static final String SERVICE_NAME = "my-service";
  private static final int RATE = 999;

  private static final AtomicInteger numPolls = new AtomicInteger();

  private final String serverName = InProcessServerBuilder.generateName();
  private final ManagedChannel inProcessChannel =
      InProcessChannelBuilder.forName(serverName).directExecutor().build();

  private final SamplingManagerGrpc.SamplingManagerImplBase service =
      mock(
          SamplingManagerGrpc.SamplingManagerImplBase.class,
          delegatesTo(new MockSamplingManagerService()));

  static class MockSamplingManagerService extends SamplingManagerGrpc.SamplingManagerImplBase {

    @Override
    public void getSamplingStrategy(
        Sampling.SamplingStrategyParameters request,
        StreamObserver<Sampling.SamplingStrategyResponse> responseObserver) {
      numPolls.incrementAndGet();
      Sampling.SamplingStrategyResponse response =
          Sampling.SamplingStrategyResponse.newBuilder()
              .setStrategyType(SamplingStrategyType.RATE_LIMITING)
              .setRateLimitingSampling(
                  RateLimitingSamplingStrategy.newBuilder().setMaxTracesPerSecond(RATE).build())
              .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }
  }

  private final Closer closer = Closer.create();

  @BeforeEach
  public void before() throws IOException {
    numPolls.set(0);
    Server server =
        InProcessServerBuilder.forName(serverName)
            .directExecutor()
            .addService(service)
            .build()
            .start();
    closer.register(server::shutdownNow);
    closer.register(inProcessChannel::shutdownNow);
  }

  @AfterEach
  void tearDown() throws Exception {
    closer.close();
  }

  @Test
  void connectionWorks() throws Exception {
    ArgumentCaptor<SamplingStrategyParameters> requestCaptor =
        ArgumentCaptor.forClass(Sampling.SamplingStrategyParameters.class);

    JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setChannel(inProcessChannel)
            .setServiceName(SERVICE_NAME)
            .build();

    await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(samplerIsType(sampler, RateLimitingSampler.class));

    // verify
    verify(service).getSamplingStrategy(requestCaptor.capture(), ArgumentMatchers.any());
    assertThat(requestCaptor.getValue().getServiceName()).isEqualTo(SERVICE_NAME);
    assertThat(sampler.getDescription()).contains("RateLimitingSampler{999.00}");

    // Default poll interval is 60s, inconceivable to have polled multiple times by now.
    assertThat(numPolls).hasValue(1);
  }

  @Test
  void description() {
    JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setChannel(inProcessChannel)
            .setServiceName(SERVICE_NAME)
            .build();
    assertThat(sampler.getDescription())
        .startsWith("JaegerRemoteSampler{ParentBased{root:TraceIdRatioBased{0.001000}");

    // wait until the sampling strategy is retrieved before exiting test method
    await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(samplerIsType(sampler, RateLimitingSampler.class));

    // Default poll interval is 60s, inconceivable to have polled multiple times by now.
    assertThat(numPolls).hasValue(1);
  }

  @Test
  void initialSampler() {
    JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setChannel(inProcessChannel)
            .setServiceName(SERVICE_NAME)
            .setInitialSampler(Sampler.alwaysOn())
            .build();
    assertThat(sampler.getDescription()).startsWith("JaegerRemoteSampler{AlwaysOnSampler}");
  }

  @Test
  void pollingInterval() throws Exception {
    JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setChannel(inProcessChannel)
            .setServiceName(SERVICE_NAME)
            .setPollingInterval(1, TimeUnit.MILLISECONDS)
            .build();

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
            .setChannel(inProcessChannel)
            .setServiceName(SERVICE_NAME)
            .setPollingInterval(Duration.ofMillis(1))
            .build();

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
    assertThatThrownBy(() -> JaegerRemoteSampler.builder().setChannel(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("channel");
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
