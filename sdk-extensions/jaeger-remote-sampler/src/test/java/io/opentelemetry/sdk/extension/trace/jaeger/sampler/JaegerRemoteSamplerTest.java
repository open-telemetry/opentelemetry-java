/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.linecorp.armeria.common.grpc.protocol.ArmeriaStatusException;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.grpc.protocol.AbstractUnaryGrpcService;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.github.netmikey.logunit.api.LogCapturer;
import io.netty.handler.ssl.ClientAuth;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.Sampling;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.Sampling.RateLimitingSamplingStrategy;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.Sampling.SamplingStrategyType;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.awaitility.core.ThrowingRunnable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

@SuppressLogger(OkHttpGrpcService.class)
class JaegerRemoteSamplerTest {

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
  LogCapturer logs = LogCapturer.create().captureForType(OkHttpGrpcService.class, Level.TRACE);

  @Order(1)
  @RegisterExtension
  static final SelfSignedCertificateExtension certificate = new SelfSignedCertificateExtension();

  @RegisterExtension
  @Order(2)
  static final SelfSignedCertificateExtension clientCertificate =
      new SelfSignedCertificateExtension();

  @Order(3)
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

                  ArmeriaStatusException grpcError = grpcErrors.peek();
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
          sb.tlsCustomizer(
              ssl -> {
                ssl.clientAuth(ClientAuth.OPTIONAL);
                ssl.trustManager(clientCertificate.certificate());
              });
        }
      };

  @BeforeEach
  public void before() {
    grpcErrors.clear();
    responses.clear();
  }

  @Test
  void connectionWorks() {
    try (JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setEndpoint(server.httpUri().toString())
            .setPollingInterval(1, TimeUnit.SECONDS)
            .setServiceName(SERVICE_NAME)
            .build()) {
      assertThat(sampler).extracting("delegate").isInstanceOf(OkHttpGrpcService.class);

      await().untilAsserted(samplerIsType(sampler, RateLimitingSampler.class));

      // verify
      assertThat(sampler.getDescription()).contains("RateLimitingSampler{999.00}");
    }
  }

  @Test
  void tlsConnectionWorks() throws IOException {
    try (JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setEndpoint(server.httpsUri().toString())
            .setPollingInterval(1, TimeUnit.SECONDS)
            .setTrustedCertificates(Files.readAllBytes(certificate.certificateFile().toPath()))
            .setServiceName(SERVICE_NAME)
            .build()) {
      assertThat(sampler).extracting("delegate").isInstanceOf(OkHttpGrpcService.class);

      await().untilAsserted(samplerIsType(sampler, RateLimitingSampler.class));

      // verify
      assertThat(sampler.getDescription()).contains("RateLimitingSampler{999.00}");
    }
  }

  @ParameterizedTest
  @ArgumentsSource(ClientPrivateKeyProvider.class)
  void clientTlsConnectionWorks(byte[] privateKey) throws IOException {
    try (JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setEndpoint(server.httpsUri().toString())
            .setPollingInterval(1, TimeUnit.SECONDS)
            .setTrustedCertificates(Files.readAllBytes(certificate.certificateFile().toPath()))
            .setClientTls(
                privateKey, Files.readAllBytes(clientCertificate.certificateFile().toPath()))
            .setServiceName(SERVICE_NAME)
            .build()) {
      assertThat(sampler).extracting("delegate").isInstanceOf(OkHttpGrpcService.class);

      await().untilAsserted(samplerIsType(sampler, RateLimitingSampler.class));

      // verify
      assertThat(sampler.getDescription()).contains("RateLimitingSampler{999.00}");
    }
  }

  private static class ClientPrivateKeyProvider implements ArgumentsProvider {
    @Override
    @SuppressWarnings("PrimitiveArrayPassedToVarargsMethod")
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return Stream.of(
          arguments(named("PEM", Files.readAllBytes(clientCertificate.privateKeyFile().toPath()))),
          arguments(named("DER", clientCertificate.privateKey().getEncoded())));
    }
  }

  @Test
  void description() {
    try (JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setEndpoint(server.httpUri().toString())
            .setPollingInterval(1, TimeUnit.SECONDS)
            .setServiceName(SERVICE_NAME)
            .build()) {
      assertThat(sampler).extracting("delegate").isInstanceOf(OkHttpGrpcService.class);

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
            .setEndpoint("http://example.com")
            .setServiceName(SERVICE_NAME)
            .setInitialSampler(Sampler.alwaysOn())
            .build()) {
      assertThat(sampler).extracting("delegate").isInstanceOf(OkHttpGrpcService.class);

      assertThat(sampler.getDescription()).startsWith("JaegerRemoteSampler{AlwaysOnSampler}");
    }
  }

  @Test
  void pollingInterval() {
    try (JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setEndpoint(server.httpUri().toString())
            .setServiceName(SERVICE_NAME)
            .setPollingInterval(1, TimeUnit.MILLISECONDS)
            .build()) {
      assertThat(sampler).extracting("delegate").isInstanceOf(OkHttpGrpcService.class);

      // wait until the sampling strategy is retrieved before exiting test method
      await().untilAsserted(samplerIsType(sampler, RateLimitingSampler.class));
    }
  }

  @Test
  void pollingInterval_duration() {
    try (JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setEndpoint(server.httpUri().toString())
            .setServiceName(SERVICE_NAME)
            .setPollingInterval(Duration.ofMillis(1))
            .build()) {
      assertThat(sampler).extracting("delegate").isInstanceOf(OkHttpGrpcService.class);

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
            .setEndpoint(server.httpUri().toString())
            .setServiceName(SERVICE_NAME)
            // Make sure only polls once.
            .setPollingInterval(500, TimeUnit.SECONDS)
            .build()) {
      assertThat(sampler).extracting("delegate").isInstanceOf(OkHttpGrpcService.class);

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
  @SuppressLogger(OkHttpGrpcService.class)
  void internal_error_server_response() {
    addGrpcError(13, "internal error");

    try (JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setEndpoint(server.httpUri().toString())
            .setServiceName(SERVICE_NAME)
            .setPollingInterval(50, TimeUnit.MILLISECONDS)
            .build()) {
      assertThat(sampler).extracting("delegate").isInstanceOf(OkHttpGrpcService.class);

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
  @SuppressLogger(OkHttpGrpcService.class)
  void unavailable_error_server_response() {
    addGrpcError(14, "クマ🐻 resource exhausted");

    try (JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setEndpoint(server.httpUri().toString())
            .setServiceName(SERVICE_NAME)
            .setPollingInterval(50, TimeUnit.MILLISECONDS)
            .build()) {
      assertThat(sampler).extracting("delegate").isInstanceOf(OkHttpGrpcService.class);

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
  @SuppressLogger(OkHttpGrpcService.class)
  void unimplemented_error_server_response() {
    addGrpcError(12, null);

    try (JaegerRemoteSampler sampler =
        JaegerRemoteSampler.builder()
            .setEndpoint(server.httpUri().toString())
            .setServiceName(SERVICE_NAME)
            .setPollingInterval(50, TimeUnit.MILLISECONDS)
            .build()) {
      assertThat(sampler).extracting("delegate").isInstanceOf(OkHttpGrpcService.class);

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

  @Test
  void builder_ValidConfig() {
    assertThatCode(() -> JaegerRemoteSampler.builder().setEndpoint("http://localhost:4317"))
        .doesNotThrowAnyException();
    assertThatCode(() -> JaegerRemoteSampler.builder().setEndpoint("http://localhost"))
        .doesNotThrowAnyException();
    assertThatCode(() -> JaegerRemoteSampler.builder().setEndpoint("https://localhost"))
        .doesNotThrowAnyException();
    assertThatCode(() -> JaegerRemoteSampler.builder().setEndpoint("http://foo:bar@localhost"))
        .doesNotThrowAnyException();

    assertThatCode(
            () ->
                JaegerRemoteSampler.builder()
                    .setTrustedCertificates(
                        Files.readAllBytes(certificate.certificateFile().toPath())))
        .doesNotThrowAnyException();

    assertThatCode(
            () ->
                JaegerRemoteSampler.builder()
                    .setClientTls(
                        Files.readAllBytes(clientCertificate.privateKeyFile().toPath()),
                        Files.readAllBytes(clientCertificate.certificateFile().toPath())))
        .doesNotThrowAnyException();
  }

  @Test
  void invalidArguments() {
    assertThatThrownBy(() -> JaegerRemoteSampler.builder().setServiceName(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("serviceName");

    assertThatThrownBy(() -> JaegerRemoteSampler.builder().setEndpoint(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("endpoint");
    assertThatThrownBy(() -> JaegerRemoteSampler.builder().setEndpoint("😺://localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must be a URL: 😺://localhost");
    assertThatThrownBy(() -> JaegerRemoteSampler.builder().setEndpoint("localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must start with http:// or https://: localhost");
    assertThatThrownBy(() -> JaegerRemoteSampler.builder().setEndpoint("gopher://localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must start with http:// or https://: gopher://localhost");

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

    assertThatThrownBy(() -> JaegerRemoteSampler.builder().setTrustedCertificates(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("trustedCertificatesPem");

    assertThatThrownBy(() -> JaegerRemoteSampler.builder().setClientTls(null, new byte[] {}))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("privateKeyPem");
    assertThatThrownBy(() -> JaegerRemoteSampler.builder().setClientTls(new byte[] {}, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("certificatePem");
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
