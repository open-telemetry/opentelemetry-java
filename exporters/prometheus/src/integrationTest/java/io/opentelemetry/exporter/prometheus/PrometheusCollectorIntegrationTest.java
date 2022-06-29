/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

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
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
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

@Testcontainers(disabledWithoutDocker = true)
class PrometheusCollectorIntegrationTest {

  private static final String COLLECTOR_IMAGE =
      "ghcr.io/open-telemetry/opentelemetry-java/otel-collector";
  private static final Integer COLLECTOR_HEALTH_CHECK_PORT = 13133;

  private static int prometheusPort;
  private static SdkMeterProvider meterProvider;
  private static OtlpGrpcServer grpcServer;
  private static GenericContainer<?> collector;

  @BeforeAll
  static void beforeAll() {
    PrometheusHttpServer prometheusHttpServer = PrometheusHttpServer.builder().setPort(0).build();
    prometheusPort = prometheusHttpServer.getAddress().getPort();
    meterProvider = SdkMeterProvider.builder().registerMetricReader(prometheusHttpServer).build();
    exposeHostPorts(prometheusPort);

    grpcServer = new OtlpGrpcServer();
    grpcServer.start();
    exposeHostPorts(grpcServer.httpPort());

    collector =
        new GenericContainer<>(DockerImageName.parse(COLLECTOR_IMAGE))
            .withEnv("APP_ENDPOINT", "host.testcontainers.internal:" + prometheusPort)
            .withEnv("LOGGING_EXPORTER_LOG_LEVEL", "INFO")
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
    Meter meter = meterProvider.meterBuilder("test").build();

    meter
        .counterBuilder("requests")
        .build()
        .add(3, Attributes.builder().put("animal", "bear").build());

    await()
        .atMost(Duration.ofSeconds(30))
        .untilAsserted(() -> assertThat(grpcServer.metricRequests.size()).isGreaterThan(0));

    ExportMetricsServiceRequest request = grpcServer.metricRequests.get(0);
    Assertions.assertThat(request.getResourceMetricsCount()).isEqualTo(1);

    ResourceMetrics resourceMetrics = request.getResourceMetrics(0);
    // TODO: when otel-collector-contrib:0.55.0 is released, update to include resource attributes
    // which it will scrape from target info and include on the OTLP resource
    assertThat(resourceMetrics.getResource().getAttributesList())
        .containsExactlyInAnyOrder(
            stringKeyValue(ResourceAttributes.SERVICE_NAME.getKey(), "app"),
            stringKeyValue(ResourceAttributes.HOST_NAME.getKey(), "host.testcontainers.internal"),
            stringKeyValue("job", "app"),
            stringKeyValue("instance", "host.testcontainers.internal:" + prometheusPort),
            stringKeyValue("port", String.valueOf(prometheusPort)),
            stringKeyValue("scheme", "http"));

    assertThat(resourceMetrics.getScopeMetricsCount()).isEqualTo(1);
    ScopeMetrics scopeMetrics = resourceMetrics.getScopeMetrics(0);
    assertThat(scopeMetrics.getScope().getName()).isEqualTo("");

    Optional<Metric> optMetric =
        scopeMetrics.getMetricsList().stream()
            .filter(metric -> metric.getName().equals("requests_total"))
            .findFirst();
    assertThat(optMetric).isPresent();
    Metric metric = optMetric.get();
    assertThat(metric.getDataCase()).isEqualTo(Metric.DataCase.SUM);

    Sum sum = metric.getSum();
    assertThat(sum.getAggregationTemporality())
        .isEqualTo(AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE);
    assertThat(sum.getDataPointsCount()).isEqualTo(1);

    NumberDataPoint dataPoint = sum.getDataPoints(0);
    assertThat(dataPoint.getAsDouble()).isEqualTo(3.0);
    assertThat(dataPoint.getAttributesList()).contains(stringKeyValue("animal", "bear"));
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
