/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.protobuf.TextFormat;
import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.client.retry.RetryRule;
import com.linecorp.armeria.client.retry.RetryingClient;
import com.linecorp.armeria.common.AggregatedHttpResponse;
import com.linecorp.armeria.common.HttpData;
import com.linecorp.armeria.common.HttpHeaderNames;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.RequestHeaders;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.CollectionRegistration;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.resources.Resource;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.exporter.httpserver.MetricsHandler;
import io.prometheus.metrics.expositionformats.generated.com_google_protobuf_4_31_1.Metrics;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.zip.GZIPInputStream;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class PrometheusHttpServerTest {
  private static final AtomicReference<List<MetricData>> metricData = new AtomicReference<>();

  @SuppressWarnings("NonFinalStaticField")
  static PrometheusHttpServer prometheusServer;

  @SuppressWarnings("NonFinalStaticField")
  static WebClient client;

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(Otel2PrometheusConverter.class);

  @BeforeAll
  static void beforeAll() {
    // Register the SDK metric producer with the prometheus reader.
    prometheusServer = PrometheusHttpServer.builder().setHost("localhost").setPort(0).build();
    prometheusServer.register(
        new CollectionRegistration() {
          @Override
          public Collection<MetricData> collectAllMetrics() {
            return metricData.get();
          }
        });

    client =
        WebClient.builder("http://localhost:" + prometheusServer.getAddress().getPort())
            .decorator(RetryingClient.newDecorator(RetryRule.failsafe()))
            .build();
  }

  @BeforeEach
  void beforeEach() {
    metricData.set(generateTestData());
  }

  @AfterAll
  static void tearDown() {
    prometheusServer.shutdown();
  }

  @SuppressWarnings("DataFlowIssue")
  @Test
  void invalidConfig() {
    assertThatThrownBy(() -> PrometheusHttpServer.builder().setPort(-1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("port must be positive");
    assertThatThrownBy(() -> PrometheusHttpServer.builder().setHost(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("host");
    assertThatThrownBy(() -> PrometheusHttpServer.builder().setHost(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("host must not be empty");
    assertThatThrownBy(() -> PrometheusHttpServer.builder().setDefaultAggregationSelector(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("defaultAggregationSelector");
  }

  @Test
  void fetchPrometheus() {
    AggregatedHttpResponse response = client.get("/metrics").aggregate().join();
    assertThat(response.status()).isEqualTo(HttpStatus.OK);
    assertThat(response.headers().get(HttpHeaderNames.CONTENT_TYPE))
        .isEqualTo("text/plain; version=0.0.4; charset=utf-8");
    assertThat(response.contentUtf8())
        .isEqualTo(
            "# HELP grpc_name_unit_total long_description\n"
                + "# TYPE grpc_name_unit_total counter\n"
                + "grpc_name_unit_total{kp=\"vp\",otel_scope_name=\"grpc\",otel_scope_version=\"version\"} 5.0\n"
                + "# HELP http_name_unit_total double_description\n"
                + "# TYPE http_name_unit_total counter\n"
                + "http_name_unit_total{kp=\"vp\",otel_scope_name=\"http\",otel_scope_version=\"version\"} 3.5\n"
                + "# TYPE target_info gauge\n"
                + "target_info{kr=\"vr\"} 1\n");
  }

  @Test
  void fetch_ReusableMemoryMode() throws InterruptedException {
    try (PrometheusHttpServer prometheusServer =
        PrometheusHttpServer.builder()
            .setHost("localhost")
            .setPort(0)
            .setMemoryMode(MemoryMode.REUSABLE_DATA)
            .build()) {
      AtomicBoolean collectInProgress = new AtomicBoolean();
      AtomicBoolean concurrentRead = new AtomicBoolean();
      prometheusServer.register(
          new CollectionRegistration() {
            @Override
            public Collection<MetricData> collectAllMetrics() {
              if (!collectInProgress.compareAndSet(false, true)) {
                concurrentRead.set(true);
              }
              Collection<MetricData> response = metricData.get();
              try {
                Thread.sleep(1);
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
              if (!collectInProgress.compareAndSet(true, false)) {
                concurrentRead.set(true);
              }
              return response;
            }
          });

      WebClient client =
          WebClient.builder("http://localhost:" + prometheusServer.getAddress().getPort())
              .decorator(RetryingClient.newDecorator(RetryRule.failsafe()))
              .build();

      // Spin up 4 threads calling /metrics simultaneously. If concurrent read happens,
      // collectAllMetrics will set concurrentRead to true and the test will fail.
      List<Thread> threads = new ArrayList<>();
      for (int i = 0; i < 4; i++) {
        Thread thread =
            new Thread(
                () -> {
                  for (int j = 0; j < 10; j++) {
                    AggregatedHttpResponse response = client.get("/metrics").aggregate().join();
                    assertThat(response.status()).isEqualTo(HttpStatus.OK);
                  }
                });
        thread.setDaemon(true);
        thread.start();
        threads.add(thread);
      }

      // Wait for threads to complete
      for (Thread thread : threads) {
        thread.join();
      }

      // Confirm no concurrent reads took place
      assertThat(concurrentRead.get()).isFalse();
    }
  }

  @Test
  void fetchOpenMetrics() {
    AggregatedHttpResponse response =
        client
            .execute(
                RequestHeaders.of(
                    HttpMethod.GET,
                    "/metrics",
                    HttpHeaderNames.ACCEPT,
                    "application/openmetrics-text"))
            .aggregate()
            .join();
    assertThat(response.status()).isEqualTo(HttpStatus.OK);
    assertThat(response.headers().get(HttpHeaderNames.CONTENT_TYPE))
        .isEqualTo("application/openmetrics-text; version=1.0.0; charset=utf-8");
    assertThat(response.contentUtf8())
        .isEqualTo(
            "# TYPE grpc_name_unit counter\n"
                + "# UNIT grpc_name_unit unit\n"
                + "# HELP grpc_name_unit long_description\n"
                + "grpc_name_unit_total{kp=\"vp\",otel_scope_name=\"grpc\",otel_scope_version=\"version\"} 5.0\n"
                + "# TYPE http_name_unit counter\n"
                + "# UNIT http_name_unit unit\n"
                + "# HELP http_name_unit double_description\n"
                + "http_name_unit_total{kp=\"vp\",otel_scope_name=\"http\",otel_scope_version=\"version\"} 3.5\n"
                + "# TYPE target info\n"
                + "target_info{kr=\"vr\"} 1\n"
                + "# EOF\n");
  }

  @Test
  void fetchProtobuf() {
    AggregatedHttpResponse response =
        client
            .execute(
                RequestHeaders.of(
                    HttpMethod.GET,
                    "/metrics",
                    HttpHeaderNames.ACCEPT,
                    "Accept: application/vnd.google.protobuf;proto=io.prometheus.client.MetricFamily"))
            .aggregate()
            .join();
    assertThat(response.status()).isEqualTo(HttpStatus.OK);
    assertThat(response.headers().get(HttpHeaderNames.CONTENT_TYPE))
        .isEqualTo(
            "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited");
    // don't decode the protobuf, just verify it doesn't throw an exception
  }

  @SuppressWarnings("ConcatenationWithEmptyString")
  @Test
  void fetchFiltered() {
    AggregatedHttpResponse response =
        client
            .get("/metrics?name[]=grpc_name_unit_total&name[]=bears_total&name[]=target_info")
            .aggregate()
            .join();
    assertThat(response.status()).isEqualTo(HttpStatus.OK);
    assertThat(response.headers().get(HttpHeaderNames.CONTENT_TYPE))
        .isEqualTo("text/plain; version=0.0.4; charset=utf-8");
    assertThat(response.contentUtf8())
        .isEqualTo(
            ""
                + "# HELP grpc_name_unit_total long_description\n"
                + "# TYPE grpc_name_unit_total counter\n"
                + "grpc_name_unit_total{kp=\"vp\",otel_scope_name=\"grpc\",otel_scope_version=\"version\"} 5.0\n"
                + "# TYPE target_info gauge\n"
                + "target_info{kr=\"vr\"} 1\n");
  }

  @Test
  void fetchOverrideDefaultHandler() {
    PrometheusRegistry registry = new PrometheusRegistry();
    try (PrometheusHttpServer prometheusServer =
        PrometheusHttpServer.builder()
            .setHost("localhost")
            .setPort(0)
            .setPrometheusRegistry(registry)
            // Set the default handler to serve metrics on /**
            .setDefaultHandler(new MetricsHandler(registry))
            .build()) {
      prometheusServer.register(
          new CollectionRegistration() {
            @Override
            public Collection<MetricData> collectAllMetrics() {
              return metricData.get();
            }
          });
      WebClient client =
          WebClient.builder("http://localhost:" + prometheusServer.getAddress().getPort())
              .decorator(RetryingClient.newDecorator(RetryRule.failsafe()))
              .build();

      // Fetch metrics from / instead of /metrics
      AggregatedHttpResponse response = client.get("/").aggregate().join();
      assertThat(response.status()).isEqualTo(HttpStatus.OK);
      assertThat(response.headers().get(HttpHeaderNames.CONTENT_TYPE))
          .isEqualTo("text/plain; version=0.0.4; charset=utf-8");
      assertThat(response.contentUtf8())
          .isEqualTo(
              "# HELP grpc_name_unit_total long_description\n"
                  + "# TYPE grpc_name_unit_total counter\n"
                  + "grpc_name_unit_total{kp=\"vp\",otel_scope_name=\"grpc\",otel_scope_version=\"version\"} 5.0\n"
                  + "# HELP http_name_unit_total double_description\n"
                  + "# TYPE http_name_unit_total counter\n"
                  + "http_name_unit_total{kp=\"vp\",otel_scope_name=\"http\",otel_scope_version=\"version\"} 3.5\n"
                  + "# TYPE target_info gauge\n"
                  + "target_info{kr=\"vr\"} 1\n");
    }
  }

  @SuppressWarnings("resource")
  @Test
  void fetchPrometheusCompressed() throws IOException {
    WebClient client =
        WebClient.builder("http://localhost:" + prometheusServer.getAddress().getPort())
            .decorator(RetryingClient.newDecorator(RetryRule.failsafe()))
            .addHeader(HttpHeaderNames.ACCEPT_ENCODING, "gzip")
            .build();
    AggregatedHttpResponse response = client.get("/metrics").aggregate().join();
    assertThat(response.status()).isEqualTo(HttpStatus.OK);
    assertThat(response.headers().get(HttpHeaderNames.CONTENT_TYPE))
        .isEqualTo("text/plain; version=0.0.4; charset=utf-8");
    assertThat(response.headers().get(HttpHeaderNames.CONTENT_ENCODING)).isEqualTo("gzip");
    GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(response.content().array()));
    String content = new String(ByteStreams.toByteArray(gis), StandardCharsets.UTF_8);
    assertThat(content)
        .isEqualTo(
            "# HELP grpc_name_unit_total long_description\n"
                + "# TYPE grpc_name_unit_total counter\n"
                + "grpc_name_unit_total{kp=\"vp\",otel_scope_name=\"grpc\",otel_scope_version=\"version\"} 5.0\n"
                + "# HELP http_name_unit_total double_description\n"
                + "# TYPE http_name_unit_total counter\n"
                + "http_name_unit_total{kp=\"vp\",otel_scope_name=\"http\",otel_scope_version=\"version\"} 3.5\n"
                + "# TYPE target_info gauge\n"
                + "target_info{kr=\"vr\"} 1\n");
  }

  @SuppressWarnings("resource")
  @Test
  void fetchHead() {
    AggregatedHttpResponse response = client.head("/metrics").aggregate().join();
    assertThat(response.status()).isEqualTo(HttpStatus.OK);
    assertThat(response.headers().get(HttpHeaderNames.CONTENT_TYPE))
        .isEqualTo("text/plain; version=0.0.4; charset=utf-8");
    assertThat(response.content().isEmpty()).isTrue();
  }

  @Test
  void fetchHealth() {
    AggregatedHttpResponse response = client.get("/-/healthy").aggregate().join();

    assertThat(response.status()).isEqualTo(HttpStatus.OK);
    assertThat(response.contentUtf8()).isEqualTo("Exporter is healthy.\n");
  }

  @Test
  @SuppressLogger(PrometheusHttpServer.class)
  @SuppressLogger(Otel2PrometheusConverter.class)
  void fetch_DuplicateMetrics() {
    Resource resource = Resource.create(Attributes.of(stringKey("kr"), "vr"));
    metricData.set(
        ImmutableList.of(
            ImmutableMetricData.createLongSum(
                resource,
                InstrumentationScopeInfo.create("scope1"),
                "foo",
                "description1",
                "unit",
                ImmutableSumData.create(
                    /* isMonotonic= */ true,
                    AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        ImmutableLongPointData.create(123, 456, Attributes.empty(), 1)))),
            // A counter with same name as foo but with different scope, description, unit
            ImmutableMetricData.createLongSum(
                resource,
                InstrumentationScopeInfo.create("scope2"),
                "foo",
                "description2",
                "unit",
                ImmutableSumData.create(
                    /* isMonotonic= */ true,
                    AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        ImmutableLongPointData.create(123, 456, Attributes.empty(), 2)))),
            // A gauge whose name conflicts with foo counter's name after the "_total" suffix is
            // added
            ImmutableMetricData.createLongGauge(
                resource,
                InstrumentationScopeInfo.create("scope3"),
                "foo_unit_total",
                "unused",
                "",
                ImmutableGaugeData.create(
                    Collections.singletonList(
                        ImmutableLongPointData.create(123, 456, Attributes.empty(), 3))))));

    AggregatedHttpResponse response = client.get("/metrics").aggregate().join();
    assertThat(response.status()).isEqualTo(HttpStatus.OK);
    assertThat(response.headers().get(HttpHeaderNames.CONTENT_TYPE))
        .isEqualTo("text/plain; version=0.0.4; charset=utf-8");
    assertThat(response.contentUtf8())
        .isEqualTo(
            "# TYPE foo_unit_total counter\n"
                + "foo_unit_total{otel_scope_name=\"scope1\"} 1.0\n"
                + "foo_unit_total{otel_scope_name=\"scope2\"} 2.0\n"
                + "# TYPE target_info gauge\n"
                + "target_info{kr=\"vr\"} 1\n");

    // Validate conflict warning message
    assertThat(logs.getEvents()).hasSize(1);
    logs.assertContains(
        "Conflicting metrics: Multiple metrics with name foo_unit but different units found. Dropping the one with unit null.");
  }

  @Test
  void stringRepresentation() {
    assertThat(prometheusServer.toString())
        .isEqualTo(
            "PrometheusHttpServer{"
                + "host=localhost,"
                + "port=0,"
                + "allowedResourceAttributesFilter=null,"
                + "memoryMode=REUSABLE_DATA,"
                + "defaultAggregationSelector=DefaultAggregationSelector{COUNTER=default, UP_DOWN_COUNTER=default, HISTOGRAM=default, OBSERVABLE_COUNTER=default, OBSERVABLE_UP_DOWN_COUNTER=default, OBSERVABLE_GAUGE=default, GAUGE=default}"
                + "}");
  }

  @Test
  void defaultExecutor() {
    assertThat(prometheusServer)
        .extracting("httpServer", as(InstanceOfAssertFactories.type(HTTPServer.class)))
        .extracting("executorService", as(InstanceOfAssertFactories.type(ThreadPoolExecutor.class)))
        .satisfies(executor -> assertThat(executor.getCorePoolSize()).isEqualTo(1));
  }

  @Test
  void customExecutor() throws IOException {
    ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(10);
    int port;
    try (ServerSocket socket = new ServerSocket(0)) {
      port = socket.getLocalPort();
    }
    try (PrometheusHttpServer server =
        PrometheusHttpServer.builder()
            .setHost("localhost")
            .setPort(port)
            // Memory mode must be IMMUTABLE_DATA to set custom executor
            .setMemoryMode(MemoryMode.IMMUTABLE_DATA)
            .setExecutor(scheduledExecutor)
            .build()) {
      assertThat(server)
          .extracting("httpServer", as(InstanceOfAssertFactories.type(HTTPServer.class)))
          .extracting(
              "executorService",
              as(InstanceOfAssertFactories.type(ScheduledThreadPoolExecutor.class)))
          .satisfies(executor -> assertThat(executor).isSameAs(scheduledExecutor));
    }
  }

  @Test
  void addResourceAttributesWorks() {
    WebClient testClient;
    try (PrometheusHttpServer testPrometheusServer =
        PrometheusHttpServer.builder()
            .setHost("localhost")
            .setPort(0)
            .setAllowedResourceAttributesFilter(Predicates.ALLOW_ALL)
            .build()) {
      testPrometheusServer.register(
          new CollectionRegistration() {
            @Override
            public Collection<MetricData> collectAllMetrics() {
              return metricData.get();
            }
          });

      testClient =
          WebClient.builder("http://localhost:" + testPrometheusServer.getAddress().getPort())
              .decorator(RetryingClient.newDecorator(RetryRule.failsafe()))
              .build();

      AggregatedHttpResponse response = testClient.get("/metrics").aggregate().join();
      assertThat(response.status()).isEqualTo(HttpStatus.OK);
      assertThat(response.headers().get(HttpHeaderNames.CONTENT_TYPE))
          .isEqualTo("text/plain; version=0.0.4; charset=utf-8");
      assertThat(response.contentUtf8())
          .isEqualTo(
              "# HELP grpc_name_unit_total long_description\n"
                  + "# TYPE grpc_name_unit_total counter\n"

                  // Note the added resource attributes as labels
                  + "grpc_name_unit_total{kp=\"vp\",kr=\"vr\",otel_scope_name=\"grpc\",otel_scope_version=\"version\"} 5.0\n"
                  + "# HELP http_name_unit_total double_description\n"
                  + "# TYPE http_name_unit_total counter\n"

                  // Note the added resource attributes as labels
                  + "http_name_unit_total{kp=\"vp\",kr=\"vr\",otel_scope_name=\"http\",otel_scope_version=\"version\"} 3.5\n"
                  + "# TYPE target_info gauge\n"
                  + "target_info{kr=\"vr\"} 1\n");
    }
  }

  private static List<MetricData> generateTestData() {
    return ImmutableList.of(
        ImmutableMetricData.createLongSum(
            Resource.create(Attributes.of(stringKey("kr"), "vr")),
            InstrumentationScopeInfo.builder("grpc").setVersion("version").build(),
            "grpc.name",
            "long_description",
            "unit",
            ImmutableSumData.create(
                /* isMonotonic= */ true,
                AggregationTemporality.CUMULATIVE,
                Collections.singletonList(
                    ImmutableLongPointData.create(
                        123, 456, Attributes.of(stringKey("kp"), "vp"), 5)))),
        ImmutableMetricData.createDoubleSum(
            Resource.create(Attributes.of(stringKey("kr"), "vr")),
            InstrumentationScopeInfo.builder("http").setVersion("version").build(),
            "http.name",
            "double_description",
            "unit",
            ImmutableSumData.create(
                /* isMonotonic= */ true,
                AggregationTemporality.CUMULATIVE,
                Collections.singletonList(
                    ImmutableDoublePointData.create(
                        123, 456, Attributes.of(stringKey("kp"), "vp"), 3.5)))));
  }

  @Test
  void toBuilder() {
    PrometheusHttpServerBuilder builder = PrometheusHttpServer.builder();
    builder.setHost("localhost");
    builder.setPort(1234);

    Predicate<String> resourceAttributesFilter = s -> false;
    builder.setAllowedResourceAttributesFilter(resourceAttributesFilter);

    ExecutorService executor = Executors.newSingleThreadExecutor();
    builder.setExecutor(executor).setMemoryMode(MemoryMode.IMMUTABLE_DATA);

    PrometheusRegistry prometheusRegistry = new PrometheusRegistry();
    builder.setPrometheusRegistry(prometheusRegistry);

    Authenticator authenticator =
        new Authenticator() {
          @Override
          public Result authenticate(HttpExchange exchange) {
            return new Success(new HttpPrincipal("anonymous", "public"));
          }
        };
    builder.setAuthenticator(authenticator);

    PrometheusHttpServer httpServer = builder.build();
    PrometheusHttpServerBuilder fromOriginalBuilder = httpServer.toBuilder();
    httpServer.close();
    assertThat(fromOriginalBuilder)
        .isInstanceOf(PrometheusHttpServerBuilder.class)
        .hasFieldOrPropertyWithValue("host", "localhost")
        .hasFieldOrPropertyWithValue("port", 1234)
        .hasFieldOrPropertyWithValue("allowedResourceAttributesFilter", resourceAttributesFilter)
        .hasFieldOrPropertyWithValue("executor", executor)
        .hasFieldOrPropertyWithValue("prometheusRegistry", prometheusRegistry)
        .hasFieldOrPropertyWithValue("authenticator", authenticator);
  }

  /**
   * Set the default histogram aggregation to be {@link
   * Aggregation#base2ExponentialBucketHistogram()}. In order to validate that exponential
   * histograms are produced, we request protobuf encoded metrics when scraping since the prometheus
   * text format does not support native histograms. We parse the binary content protobuf payload to
   * the protobuf java bindings, and assert against the string representation.
   */
  @Test
  void histogramDefaultBase2ExponentialHistogram() throws IOException {
    PrometheusHttpServer prometheusServer =
        PrometheusHttpServer.builder()
            .setHost("localhost")
            .setPort(0)
            .setDefaultAggregationSelector(
                DefaultAggregationSelector.getDefault()
                    .with(InstrumentType.HISTOGRAM, Aggregation.base2ExponentialBucketHistogram()))
            .build();
    try (SdkMeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(prometheusServer).build()) {
      DoubleHistogram histogram = meterProvider.get("meter").histogramBuilder("histogram").build();
      histogram.record(1.0);

      WebClient client =
          WebClient.builder("http://localhost:" + prometheusServer.getAddress().getPort())
              .decorator(RetryingClient.newDecorator(RetryRule.failsafe()))
              // Request protobuf binary encoding, which is required for the prometheus native
              // histogram format
              .addHeader(
                  "Accept",
                  "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily")
              .build();
      AggregatedHttpResponse response = client.get("/metrics").aggregate().join();
      assertThat(response.status()).isEqualTo(HttpStatus.OK);
      assertThat(response.headers().get(HttpHeaderNames.CONTENT_TYPE))
          .isEqualTo(
              "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited");
      // Parse the data to Metrics.MetricFamily protobuf java binding and assert against the string
      // representation
      try (HttpData data = response.content()) {
        Metrics.MetricFamily metricFamily =
            Metrics.MetricFamily.parseDelimitedFrom(data.toInputStream());
        String s = TextFormat.printer().printToString(metricFamily);
        assertThat(s)
            .isEqualTo(
                "name: \"histogram\"\n"
                    + "help: \"\"\n"
                    + "type: HISTOGRAM\n"
                    + "metric {\n"
                    + "  label {\n"
                    + "    name: \"otel_scope_name\"\n"
                    + "    value: \"meter\"\n"
                    + "  }\n"
                    + "  histogram {\n"
                    + "    sample_count: 1\n"
                    + "    sample_sum: 1.0\n"
                    + "    schema: 8\n"
                    + "    zero_threshold: 0.0\n"
                    + "    zero_count: 0\n"
                    + "    positive_span {\n"
                    + "      offset: 0\n"
                    + "      length: 1\n"
                    + "    }\n"
                    + "    positive_delta: 1\n"
                    + "  }\n"
                    + "}\n");
      }
    } finally {
      prometheusServer.shutdown();
    }
  }

  @Test
  void authenticator() {
    // given
    try (PrometheusHttpServer prometheusServer =
        PrometheusHttpServer.builder()
            .setHost("localhost")
            .setPort(0)
            .setAuthenticator(
                new Authenticator() {
                  @Override
                  public Result authenticate(HttpExchange exchange) {
                    if ("/metrics".equals(exchange.getRequestURI().getPath())) {
                      return new Failure(401);
                    } else {
                      return new Success(new HttpPrincipal("anonymous", "public"));
                    }
                  }
                })
            .build()) {
      prometheusServer.register(
          new CollectionRegistration() {
            @Override
            public Collection<MetricData> collectAllMetrics() {
              return metricData.get();
            }
          });
      WebClient client =
          WebClient.builder("http://localhost:" + prometheusServer.getAddress().getPort())
              .decorator(RetryingClient.newDecorator(RetryRule.failsafe()))
              .build();

      // when
      AggregatedHttpResponse metricsResponse = client.get("/metrics").aggregate().join();
      AggregatedHttpResponse defaultResponse = client.get("/").aggregate().join();

      // then
      assertThat(metricsResponse.status()).isEqualTo(HttpStatus.UNAUTHORIZED);
      assertThat(defaultResponse.status()).isEqualTo(HttpStatus.OK);
    }
  }
}
