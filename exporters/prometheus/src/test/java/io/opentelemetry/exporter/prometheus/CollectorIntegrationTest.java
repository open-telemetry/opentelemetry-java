/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.testcontainers.Testcontainers.exposeHostPorts;

import com.google.protobuf.InvalidProtocolBufferException;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.grpc.protocol.AbstractUnaryGrpcService;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.metrics.v1.AggregationTemporality;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.NumberDataPoint;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.ScopeMetrics;
import io.opentelemetry.proto.metrics.v1.Sum;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Verifies {@link PrometheusHttpServer} end to end using the OpenTelemetry Collector. The Collector
 * is configured to scrape the {@link PrometheusHttpServer} and export data over gRPC to a server
 * running in process, allowing assertions to be made against the data.
 */
@Testcontainers(disabledWithoutDocker = true)
@SuppressWarnings("NonFinalStaticField")
class CollectorIntegrationTest {

  private static final String COLLECTOR_IMAGE =
      "otel/opentelemetry-collector-contrib:0.139.0@sha256:faf125d656fa47cea568b2f3b4494efd2525083bc75c1e96038bc23f05cd68fd";

  private static final Integer COLLECTOR_HEALTH_CHECK_PORT = 13133;

  private static int prometheusPort;
  private static Resource resource;
  private static SdkMeterProvider meterProvider;
  private static OtlpGrpcServer grpcServer;
  private static GenericContainer<?> collector;

  @BeforeAll
  static void beforeAll() {
    PrometheusHttpServer prometheusHttpServer = PrometheusHttpServer.builder().setPort(0).build();
    prometheusPort = prometheusHttpServer.getAddress().getPort();
    resource = Resource.getDefault();
    meterProvider =
        SdkMeterProvider.builder()
            .setResource(resource)
            .registerMetricReader(prometheusHttpServer)
            .build();
    exposeHostPorts(prometheusPort);

    grpcServer = new OtlpGrpcServer();
    grpcServer.start();
    exposeHostPorts(grpcServer.httpPort());

    collector =
        new GenericContainer<>(DockerImageName.parse(COLLECTOR_IMAGE))
            .withEnv("APP_ENDPOINT", "host.testcontainers.internal:" + prometheusPort)
            .withEnv("LOGGING_EXPORTER_VERBOSITY", "detailed")
            .withEnv(
                "OTLP_EXPORTER_ENDPOINT", "host.testcontainers.internal:" + grpcServer.httpPort())
            .withClasspathResourceMapping(
                "otel-config.yaml", "/otel-config.yaml", BindMode.READ_ONLY)
            .withCommand("--config", "/otel-config.yaml")
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("otel-collector")))
            .withExposedPorts(COLLECTOR_HEALTH_CHECK_PORT)
            .waitingFor(Wait.forHttp("/").forPort(COLLECTOR_HEALTH_CHECK_PORT));
    collector.start();
  }

  @AfterAll
  static void afterAll() {
    meterProvider.shutdown().join(10, TimeUnit.SECONDS);
    grpcServer.stop().join();
    collector.stop();
  }

  @AfterEach
  void afterEach() {
    grpcServer.reset();
  }

  @Test
  void endToEnd() {
    Meter meter = meterProvider.meterBuilder("test").setInstrumentationVersion("1.0.0").build();

    meter
        .counterBuilder("requests")
        .build()
        .add(3, Attributes.builder().put("animal", "bear").build());

    await()
        .atMost(Duration.ofSeconds(30))
        .untilAsserted(() -> assertThat(grpcServer.metricRequests.size()).isGreaterThan(0));

    ExportMetricsServiceRequest request = grpcServer.metricRequests.get(0);
    assertThat(request.getResourceMetricsCount()).isEqualTo(1);

    ResourceMetrics resourceMetrics = request.getResourceMetrics(0);
    assertThat(resourceMetrics.getResource().getAttributesList())
        .containsExactlyInAnyOrder(
            // Resource attributes derived from the prometheus scrape config
            stringKeyValue("service.name", "app"),
            stringKeyValue("service.instance.id", "host.testcontainers.internal:" + prometheusPort),
            stringKeyValue("server.address", "host.testcontainers.internal"),
            stringKeyValue("server.port", String.valueOf(prometheusPort)),
            stringKeyValue("url.scheme", "http"),
            // Resource attributes from the metric SDK resource translated to target_info
            stringKeyValue(
                "service_name",
                Objects.requireNonNull(resource.getAttributes().get(stringKey("service.name")))),
            stringKeyValue(
                "telemetry_sdk_name",
                Objects.requireNonNull(
                    resource.getAttributes().get(stringKey("telemetry.sdk.name")))),
            stringKeyValue(
                "telemetry_sdk_language",
                Objects.requireNonNull(
                    resource.getAttributes().get(stringKey("telemetry.sdk.language")))),
            stringKeyValue(
                "telemetry_sdk_version",
                Objects.requireNonNull(
                    resource.getAttributes().get(stringKey("telemetry.sdk.version")))));

    assertThat(resourceMetrics.getScopeMetricsCount()).isEqualTo(2);
    ScopeMetrics testScopeMetrics =
        resourceMetrics.getScopeMetricsList().stream()
            .filter(scopeMetrics -> scopeMetrics.getScope().getName().equals("test"))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("missing scope with name \"test\""));
    assertThat(testScopeMetrics.getScope().getVersion()).isEqualTo("1.0.0");

    Optional<Metric> optRequestTotal =
        testScopeMetrics.getMetricsList().stream()
            .filter(metric -> metric.getName().equals("requests_total"))
            .findFirst();
    assertThat(optRequestTotal).isPresent();
    Metric requestTotal = optRequestTotal.get();
    assertThat(requestTotal.getDataCase()).isEqualTo(Metric.DataCase.SUM);

    Sum requestTotalSum = requestTotal.getSum();
    assertThat(requestTotalSum.getAggregationTemporality())
        .isEqualTo(AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE);
    assertThat(requestTotalSum.getIsMonotonic()).isTrue();
    assertThat(requestTotalSum.getDataPointsCount()).isEqualTo(1);

    NumberDataPoint requestTotalDataPoint = requestTotalSum.getDataPoints(0);
    assertThat(requestTotalDataPoint.getAsDouble()).isEqualTo(3.0);
    assertThat(requestTotalDataPoint.getAttributesList())
        .containsExactlyInAnyOrder(stringKeyValue("animal", "bear"));
    // Scope name and version are serialized as attributes to disambiguate metrics with the
    // same name in different scopes
    // TODO: potentially add these back or remove entirely, see
    // https://github.com/open-telemetry/opentelemetry-java/issues/7544
    // stringKeyValue("otel_scope_name", "test"),
    // stringKeyValue("otel_scope_version", "1.0.0"));
  }

  private static KeyValue stringKeyValue(String key, String value) {
    return KeyValue.newBuilder()
        .setKey(key)
        .setValue(AnyValue.newBuilder().setStringValue(value).build())
        .build();
  }

  private static class OtlpGrpcServer extends ServerExtension {

    private final List<ExportMetricsServiceRequest> metricRequests = new ArrayList<>();

    private void reset() {
      metricRequests.clear();
    }

    @Override
    protected void configure(ServerBuilder sb) {
      sb.service(
          "/opentelemetry.proto.collector.metrics.v1.MetricsService/Export",
          new AbstractUnaryGrpcService() {
            @Override
            protected CompletionStage<byte[]> handleMessage(
                ServiceRequestContext ctx, byte[] message) {
              try {
                metricRequests.add(ExportMetricsServiceRequest.parseFrom(message));
              } catch (InvalidProtocolBufferException e) {
                throw new UncheckedIOException(e);
              }
              return completedFuture(
                  ExportMetricsServiceResponse.getDefaultInstance().toByteArray());
            }
          });
      sb.http(0);
    }
  }
}
