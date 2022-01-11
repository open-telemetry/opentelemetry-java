/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import com.google.common.io.Closer;
import com.linecorp.armeria.common.grpc.protocol.ArmeriaStatusException;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.grpc.protocol.AbstractUnaryGrpcService;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.Sampling;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.Sampling.RateLimitingSamplingStrategy;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.Sampling.SamplingStrategyType;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import org.awaitility.core.ThrowingRunnable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

class JaegerRemoteSamplerTest {

  private static final String SERVICE_NAME = "my-service";
  private static final int RATE = 999;

  private static final AtomicInteger numPolls = new AtomicInteger();
  private static final ConcurrentLinkedQueue<ArmeriaStatusException> grpcErrors =
      new ConcurrentLinkedQueue<>();

  private static final ConcurrentLinkedQueue<Sampling.SamplingStrategyResponse> responses =
      new ConcurrentLinkedQueue<>();

  private static void addGrpcError(int code, @Nullable String message) {
    grpcErrors.add(new ArmeriaStatusException(code, message));
  }

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(OkHttpGrpcService.class, Level.TRACE);

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

                  numPolls.incrementAndGet();
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
          sb.https(0);
          sb.tls(certificate.certificateFile(), certificate.privateKeyFile());
        }
      };

  private final Closer closer = Closer.create();

  @BeforeEach
  public void before() {
    numPolls.set(0);
    grpcErrors.clear();
    responses.clear();
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
  void tlsConnectionWorks() throws IOException {
    JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setEndpoint(server.httpsUri().toString())
            .setPollingInterval(1, TimeUnit.SECONDS)
            .setTrustedCertificates(Files.readAllBytes(certificate.certificateFile().toPath()))
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

    JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setEndpoint(server.httpUri().toString())
            .setServiceName(SERVICE_NAME)
            .setPollingInterval(500, TimeUnit.SECONDS)
            .build();
    closer.register(sampler);

    await().atMost(Duration.ofSeconds(10)).until(() -> numPolls.get() > 0);
    assertThat(numPolls).hasValueGreaterThanOrEqualTo(1);
    // wait until correct response is returned
    assertThat(sampler.getDescription())
        .startsWith(
            "JaegerRemoteSampler{ParentBased{root:PerOperationSampler{default=TraceIdRatioBased{0.550000}, perOperation={foo=TraceIdRatioBased{0.900000}, bar=TraceIdRatioBased{0.700000}}}");
    assertThat(sampler.getDescription()).contains("bar");
  }

  @Test
  void internal_error_server_response() {
    addGrpcError(13, "internal error");

    JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setEndpoint(server.httpUri().toString())
            .setServiceName(SERVICE_NAME)
            .setPollingInterval(200, TimeUnit.MILLISECONDS)
            .build();
    closer.register(sampler);

    assertThat(sampler.getDescription())
        .startsWith("JaegerRemoteSampler{ParentBased{root:TraceIdRatioBased{0.001000}");

    await().atMost(Duration.ofSeconds(10)).until(() -> numPolls.get() > 2);
    assertThat(numPolls).hasValueGreaterThanOrEqualTo(2);
    // wait until correct response is returned
    assertThat(sampler.getDescription())
        .startsWith("JaegerRemoteSampler{ParentBased{root:RateLimitingSampler{999.00}");

    LoggingEvent log = logs.assertContains(" Server responded with gRPC status code 13");
    assertThat(log.getLevel()).isEqualTo(Level.WARN);
  }

  @Test
  void unavailable_error_server_response() {
    addGrpcError(14, "クマ🐻 resource exhausted");

    JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setEndpoint(server.httpUri().toString())
            .setServiceName(SERVICE_NAME)
            .setPollingInterval(200, TimeUnit.MILLISECONDS)
            .build();
    closer.register(sampler);

    assertThat(sampler.getDescription())
        .startsWith("JaegerRemoteSampler{ParentBased{root:TraceIdRatioBased{0.001000}");

    await().atMost(Duration.ofSeconds(10)).until(() -> numPolls.get() > 2);
    assertThat(numPolls).hasValueGreaterThanOrEqualTo(2);
    // wait until correct response is returned
    assertThat(sampler.getDescription())
        .startsWith("JaegerRemoteSampler{ParentBased{root:RateLimitingSampler{999.00}");

    LoggingEvent log = logs.assertContains("Server is UNAVAILABLE");
    assertThat(log.getLevel()).isEqualTo(Level.ERROR);
  }

  @Test
  void unimplemented_error_server_response() {
    addGrpcError(12, null);

    JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setEndpoint(server.httpUri().toString())
            .setServiceName(SERVICE_NAME)
            .setPollingInterval(200, TimeUnit.MILLISECONDS)
            .build();
    closer.register(sampler);

    assertThat(sampler.getDescription())
        .startsWith("JaegerRemoteSampler{ParentBased{root:TraceIdRatioBased{0.001000}");

    await().atMost(Duration.ofSeconds(10)).until(() -> numPolls.get() > 2);
    assertThat(numPolls).hasValueGreaterThanOrEqualTo(2);
    // wait until correct response is returned
    assertThat(sampler.getDescription())
        .startsWith("JaegerRemoteSampler{ParentBased{root:RateLimitingSampler{999.00}");

    LoggingEvent log = logs.assertContains("Server responded with UNIMPLEMENTED.");
    assertThat(log.getLevel()).isEqualTo(Level.ERROR);
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
  void usingOkHttp() {
    assertThat(JaegerRemoteSampler.builder().getDelegate())
        .isInstanceOf(OkHttpGrpcServiceBuilder.class);
  }

  static ThrowingRunnable samplerIsType(
      JaegerRemoteSampler sampler, Class<? extends Sampler> expected) {
    return () -> {
      assertThat(sampler.getSampler().getClass().getName())
          .isEqualTo("io.opentelemetry.sdk.trace.samplers.ParentBasedSampler");

      Field field = sampler.getSampler().getClass().getDeclaredField("root");
      field.setAccessible(true);
      assertThat(field.get(sampler.getSampler()).getClass()).isEqualTo(expected);
    };
  }
}
