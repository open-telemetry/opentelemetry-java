/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.jaeger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

import com.google.protobuf.InvalidProtocolBufferException;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.grpc.protocol.AbstractUnaryGrpcService;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.exporter.internal.grpc.OkHttpGrpcExporter;
import io.opentelemetry.exporter.jaeger.proto.api_v2.Collector;
import io.opentelemetry.exporter.jaeger.proto.api_v2.Model;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class JaegerGrpcSpanExporterTest {
  private static final BlockingQueue<Collector.PostSpansRequest> postedRequests =
      new LinkedBlockingDeque<>();

  @RegisterExtension
  static final ServerExtension server =
      new ServerExtension() {
        @Override
        protected void configure(ServerBuilder sb) {
          sb.service(
              JaegerGrpcSpanExporterBuilder.GRPC_ENDPOINT_PATH,
              new AbstractUnaryGrpcService() {
                @Override
                protected CompletionStage<byte[]> handleMessage(
                    ServiceRequestContext ctx, byte[] message) {
                  try {
                    postedRequests.add(Collector.PostSpansRequest.parseFrom(message));
                  } catch (InvalidProtocolBufferException e) {
                    CompletableFuture<byte[]> future = new CompletableFuture<>();
                    future.completeExceptionally(e);
                    return future;
                  }
                  return CompletableFuture.completedFuture(
                      Collector.PostSpansResponse.getDefaultInstance().toByteArray());
                }
              });
        }
      };

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(OkHttpGrpcExporter.class);

  @RegisterExtension
  static final SelfSignedCertificateExtension serverTls = new SelfSignedCertificateExtension();

  @RegisterExtension
  static final SelfSignedCertificateExtension clientTls = new SelfSignedCertificateExtension();

  private static JaegerGrpcSpanExporter exporter;

  @BeforeAll
  static void setUp() {
    exporter =
        JaegerGrpcSpanExporter.builder()
            .setEndpoint(server.httpUri().toString())
            .setMeterProvider(MeterProvider.noop())
            .build();
  }

  @AfterAll
  static void tearDown() {
    exporter.shutdown();
  }

  @AfterEach
  void reset() {
    postedRequests.clear();
  }

  @Test
  void testExport() throws Exception {
    SpanData span =
        testSpanData(
            Resource.create(
                Attributes.of(
                    ResourceAttributes.SERVICE_NAME,
                    "myServiceName",
                    AttributeKey.stringKey("resource-attr-key"),
                    "resource-attr-value")),
            "GET /api/endpoint");

    // test
    CompletableResultCode result = exporter.export(Collections.singletonList(span));
    result.join(10, TimeUnit.SECONDS);
    assertThat(result.isSuccess()).isEqualTo(true);

    // verify
    assertThat(postedRequests).hasSize(1);
    Model.Batch batch = postedRequests.poll().getBatch();
    assertThat(batch.getSpans(0).getOperationName()).isEqualTo("GET /api/endpoint");
    assertThat(SpanId.fromBytes(batch.getSpans(0).getSpanId().toByteArray()))
        .isEqualTo(span.getSpanContext().getSpanId());

    assertThat(
            getTagValue(batch.getProcess().getTagsList(), "resource-attr-key")
                .orElseThrow(() -> new AssertionError("resource-attr-key not found"))
                .getVStr())
        .isEqualTo("resource-attr-value");

    verifyBatch(batch);
    assertThat(batch.getProcess().getServiceName()).isEqualTo("myServiceName");
  }

  @Test
  void testExportMultipleResources() throws Exception {
    SpanData span =
        testSpanData(
            Resource.create(
                Attributes.of(
                    ResourceAttributes.SERVICE_NAME,
                    "myServiceName1",
                    AttributeKey.stringKey("resource-attr-key-1"),
                    "resource-attr-value-1")),
            "GET /api/endpoint/1");

    SpanData span2 =
        testSpanData(
            Resource.create(
                Attributes.of(
                    ResourceAttributes.SERVICE_NAME,
                    "myServiceName2",
                    AttributeKey.stringKey("resource-attr-key-2"),
                    "resource-attr-value-2")),
            "GET /api/endpoint/2");

    // test
    CompletableResultCode result = exporter.export(Arrays.asList(span, span2));
    result.join(10, TimeUnit.SECONDS);
    assertThat(result.isSuccess()).isEqualTo(true);

    // verify
    assertThat(postedRequests).hasSize(2);
    List<Collector.PostSpansRequest> requests = new ArrayList<>(postedRequests);
    assertThat(requests).hasSize(2);
    for (Collector.PostSpansRequest request : requests) {
      Model.Batch batch = request.getBatch();

      verifyBatch(batch);

      Optional<Model.KeyValue> processTag =
          getTagValue(batch.getProcess().getTagsList(), "resource-attr-key-1");
      Optional<Model.KeyValue> processTag2 =
          getTagValue(batch.getProcess().getTagsList(), "resource-attr-key-2");
      if (processTag.isPresent()) {
        assertThat(processTag2.isPresent()).isFalse();
        assertThat(batch.getSpans(0).getOperationName()).isEqualTo("GET /api/endpoint/1");
        assertThat(SpanId.fromBytes(batch.getSpans(0).getSpanId().toByteArray()))
            .isEqualTo(span.getSpanContext().getSpanId());
        assertThat(processTag.get().getVStr()).isEqualTo("resource-attr-value-1");
        assertThat(batch.getProcess().getServiceName()).isEqualTo("myServiceName1");
      } else if (processTag2.isPresent()) {
        assertThat(batch.getSpans(0).getOperationName()).isEqualTo("GET /api/endpoint/2");
        assertThat(SpanId.fromBytes(batch.getSpans(0).getSpanId().toByteArray()))
            .isEqualTo(span2.getSpanContext().getSpanId());
        assertThat(processTag2.get().getVStr()).isEqualTo("resource-attr-value-2");
        assertThat(batch.getProcess().getServiceName()).isEqualTo("myServiceName2");
      } else {
        fail("No process tag resource-attr-key-1 or resource-attr-key-2");
      }
    }
  }

  private static void verifyBatch(Model.Batch batch) throws Exception {
    assertThat(batch.getSpansCount()).isEqualTo(1);
    assertThat(TraceId.fromBytes(batch.getSpans(0).getTraceId().toByteArray())).isNotNull();
    assertThat(batch.getProcess().getTagsCount()).isEqualTo(5);

    assertThat(
            getSpanTagValue(batch.getSpans(0), "otel.scope.name")
                .orElseThrow(() -> new AssertionError("otel.scope.name not found"))
                .getVStr())
        .isEqualTo("io.opentelemetry.auto");

    assertThat(
            getSpanTagValue(batch.getSpans(0), "otel.library.name")
                .orElseThrow(() -> new AssertionError("otel.library.name not found"))
                .getVStr())
        .isEqualTo("io.opentelemetry.auto");

    assertThat(
            getSpanTagValue(batch.getSpans(0), "otel.library.version")
                .orElseThrow(() -> new AssertionError("otel.library.version not found"))
                .getVStr())
        .isEqualTo("1.0.0");

    assertThat(
            getSpanTagValue(batch.getSpans(0), "otel.scope.version")
                .orElseThrow(() -> new AssertionError("otel.scope.version not found"))
                .getVStr())
        .isEqualTo("1.0.0");

    assertThat(
            getTagValue(batch.getProcess().getTagsList(), "ip")
                .orElseThrow(() -> new AssertionError("ip not found"))
                .getVStr())
        .isEqualTo(exporter.getJaegerResource().getAttribute(JaegerGrpcSpanExporter.IP_KEY));

    assertThat(
            getTagValue(batch.getProcess().getTagsList(), "hostname")
                .orElseThrow(() -> new AssertionError("hostname not found"))
                .getVStr())
        .isEqualTo(InetAddress.getLocalHost().getHostName());

    assertThat(
            getTagValue(batch.getProcess().getTagsList(), "jaeger.version")
                .orElseThrow(() -> new AssertionError("jaeger.version not found"))
                .getVStr())
        .isEqualTo("opentelemetry-java");
  }

  private static Optional<Model.KeyValue> getSpanTagValue(Model.Span span, String tagKey) {
    return getTagValue(span.getTagsList(), tagKey);
  }

  private static Optional<Model.KeyValue> getTagValue(List<Model.KeyValue> tags, String tagKey) {
    return tags.stream().filter(kv -> kv.getKey().equals(tagKey)).findFirst();
  }

  private static SpanData testSpanData(Resource resource, String spanName) {
    long duration = 900; // ms
    long startMs = System.currentTimeMillis();
    long endMs = startMs + duration;
    return TestSpanData.builder()
        .setHasEnded(true)
        .setSpanContext(
            SpanContext.create(
                IdGenerator.random().generateTraceId(),
                IdGenerator.random().generateSpanId(),
                TraceFlags.getSampled(),
                TraceState.getDefault()))
        .setName(spanName)
        .setStartEpochNanos(TimeUnit.MILLISECONDS.toNanos(startMs))
        .setEndEpochNanos(TimeUnit.MILLISECONDS.toNanos(endMs))
        .setStatus(StatusData.ok())
        .setKind(SpanKind.CONSUMER)
        .setLinks(Collections.emptyList())
        .setTotalRecordedLinks(0)
        .setTotalRecordedEvents(0)
        .setInstrumentationScopeInfo(
            InstrumentationScopeInfo.builder("io.opentelemetry.auto").setVersion("1.0.0").build())
        .setResource(resource)
        .build();
  }

  @Test
  void validTrustedConfig() throws Exception {
    assertThatCode(
            () ->
                JaegerGrpcSpanExporter.builder()
                    .setTrustedCertificates(serverTls.certificate().getEncoded()))
        .doesNotThrowAnyException();
  }

  @Test
  void validClientKeyConfig() throws Exception {
    assertThatCode(
            () ->
                JaegerGrpcSpanExporter.builder()
                    .setClientTls(
                        clientTls.privateKey().getEncoded(), serverTls.certificate().getEncoded()))
        .doesNotThrowAnyException();
  }

  @Test
  @SuppressWarnings("PreferJavaTimeOverload")
  void invalidConfig() {
    assertThatThrownBy(() -> JaegerGrpcSpanExporter.builder().setTimeout(-1, TimeUnit.MILLISECONDS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("timeout must be non-negative");
    assertThatThrownBy(() -> JaegerGrpcSpanExporter.builder().setTimeout(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
    assertThatThrownBy(() -> JaegerGrpcSpanExporter.builder().setTimeout(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("timeout");

    assertThatThrownBy(() -> JaegerGrpcSpanExporter.builder().setEndpoint(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("endpoint");
    assertThatThrownBy(() -> JaegerGrpcSpanExporter.builder().setEndpoint("😺://localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must be a URL: 😺://localhost")
        .hasCauseInstanceOf(URISyntaxException.class);
    assertThatThrownBy(() -> JaegerGrpcSpanExporter.builder().setEndpoint("localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must start with http:// or https://: localhost");
    assertThatThrownBy(() -> JaegerGrpcSpanExporter.builder().setEndpoint("gopher://localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must start with http:// or https://: gopher://localhost");

    assertThatThrownBy(() -> JaegerGrpcSpanExporter.builder().setCompression(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("compressionMethod");
    assertThatThrownBy(() -> JaegerGrpcSpanExporter.builder().setCompression("foo"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Unsupported compression method. Supported compression methods include: gzip, none.");
  }

  @Test
  void compressionDefault() {
    JaegerGrpcSpanExporter exporter = JaegerGrpcSpanExporter.builder().build();
    try {
      assertThat(exporter).extracting("delegate.compressionEnabled").isEqualTo(false);
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void compressionNone() {
    JaegerGrpcSpanExporter exporter =
        JaegerGrpcSpanExporter.builder().setCompression("none").build();
    try {
      assertThat(exporter).extracting("delegate.compressionEnabled").isEqualTo(false);
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void compressionGzip() {
    JaegerGrpcSpanExporter exporter =
        JaegerGrpcSpanExporter.builder().setCompression("gzip").build();
    try {
      assertThat(exporter).extracting("delegate.compressionEnabled").isEqualTo(true);
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void compressionEnabledAndDisabled() {
    JaegerGrpcSpanExporter exporter =
        JaegerGrpcSpanExporter.builder().setCompression("gzip").setCompression("none").build();
    try {
      assertThat(exporter).extracting("delegate.compressionEnabled").isEqualTo(false);
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  @SuppressLogger(OkHttpGrpcExporter.class)
  void shutdown() {
    JaegerGrpcSpanExporter exporter =
        JaegerGrpcSpanExporter.builder().setEndpoint(server.httpUri().toString()).build();
    assertThat(exporter.shutdown().join(1, TimeUnit.SECONDS).isSuccess()).isTrue();
    assertThat(logs.getEvents()).isEmpty();
    assertThat(
            exporter
                .export(Collections.singletonList(testSpanData(Resource.getDefault(), "span name")))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isFalse();
    assertThat(exporter.shutdown().join(1, TimeUnit.SECONDS).isSuccess()).isTrue();
    logs.assertContains("Calling shutdown() multiple times.");
  }
}
