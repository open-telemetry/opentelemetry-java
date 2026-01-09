/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin;

import static io.opentelemetry.exporter.zipkin.ZipkinTestUtil.spanBuilder;
import static io.opentelemetry.exporter.zipkin.ZipkinTestUtil.zipkinSpanBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.context.Context;
import io.opentelemetry.exporter.zipkin.internal.copied.InstrumentationUtil;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zipkin2.Span;
import zipkin2.reporter.BytesEncoder;
import zipkin2.reporter.BytesMessageSender;
import zipkin2.reporter.Encoding;
import zipkin2.reporter.SpanBytesEncoder;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("deprecation") // testing deprecated code
class ZipkinSpanExporterTest {

  @Mock private BytesMessageSender mockSender;
  @Mock private SpanBytesEncoder mockEncoder;
  @Mock private OtelToZipkinSpanTransformer mockTransformer;
  @Mock private InetAddress localIp;

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(ZipkinSpanExporter.class);

  @Test
  void testExport() throws IOException {
    TestSpanData testSpanData = spanBuilder().build();

    ZipkinSpanExporter zipkinSpanExporter =
        new ZipkinSpanExporter(
            new ZipkinSpanExporterBuilder(),
            mockEncoder,
            mockSender,
            MeterProvider::noop,
            InternalTelemetryVersion.LATEST,
            "http://testing:1234",
            mockTransformer);

    byte[] someBytes = new byte[0];
    Span zipkinSpan =
        zipkinSpanBuilder(Span.Kind.SERVER, localIp)
            .putTag(OtelToZipkinSpanTransformer.OTEL_STATUS_CODE, "OK")
            .build();
    when(mockTransformer.generateSpan(testSpanData)).thenReturn(zipkinSpan);
    when(mockEncoder.encode(zipkinSpan)).thenReturn(someBytes);

    CompletableResultCode resultCode =
        zipkinSpanExporter.export(Collections.singleton(testSpanData));

    assertThat(resultCode.isSuccess()).isTrue();

    verify(mockSender).send(Collections.singletonList(someBytes));
  }

  @Test
  @SuppressLogger(ZipkinSpanExporter.class)
  void testExport_failed() throws IOException {
    TestSpanData testSpanData = spanBuilder().build();

    ZipkinSpanExporter zipkinSpanExporter =
        new ZipkinSpanExporter(
            new ZipkinSpanExporterBuilder(),
            mockEncoder,
            mockSender,
            MeterProvider::noop,
            InternalTelemetryVersion.LATEST,
            "http://testing:1234",
            mockTransformer);

    byte[] someBytes = new byte[0];
    Span zipkinSpan =
        zipkinSpanBuilder(Span.Kind.SERVER, localIp)
            .putTag(OtelToZipkinSpanTransformer.OTEL_STATUS_CODE, "OK")
            .build();
    when(mockTransformer.generateSpan(testSpanData)).thenReturn(zipkinSpan);
    when(mockEncoder.encode(zipkinSpan)).thenReturn(someBytes);
    doThrow(new IOException()).when(mockSender).send(Collections.singletonList(someBytes));

    CompletableResultCode resultCode =
        zipkinSpanExporter.export(Collections.singleton(testSpanData));

    assertThat(resultCode.isSuccess()).isFalse();

    verify(mockSender).send(Collections.singletonList(someBytes));
  }

  @Test
  void testCreate() {
    ZipkinSpanExporter exporter = ZipkinSpanExporter.builder().setSender(mockSender).build();

    assertThat(exporter).isNotNull();
  }

  @Test
  @SuppressLogger(ZipkinSpanExporter.class)
  void testShutdown() throws IOException {
    ZipkinSpanExporter exporter = ZipkinSpanExporter.builder().setSender(mockSender).build();

    assertThat(exporter.shutdown().isSuccess()).isTrue();
    verify(mockSender).close();
    assertThat(logs.getEvents()).isEmpty();
    assertThat(
            exporter
                .export(Collections.singletonList(spanBuilder().build()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isFalse();
    assertThat(exporter.shutdown().isSuccess()).isTrue();
    logs.assertContains("Calling shutdown() multiple times.");
  }

  @Test
  @SuppressWarnings({"PreferJavaTimeOverload", "deprecation"})
  // we have to use the deprecated setEncoder overload to test it
  void invalidConfig() {
    assertThatThrownBy(() -> ZipkinSpanExporter.builder().setReadTimeout(-1, TimeUnit.MILLISECONDS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("timeout must be non-negative");

    assertThatThrownBy(() -> ZipkinSpanExporter.builder().setReadTimeout(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");

    assertThatThrownBy(() -> ZipkinSpanExporter.builder().setReadTimeout(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("timeout");

    assertThatThrownBy(() -> ZipkinSpanExporter.builder().setEndpoint(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("endpoint");

    assertThatThrownBy(() -> ZipkinSpanExporter.builder().setCompression(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("compressionMethod");

    assertThatThrownBy(() -> ZipkinSpanExporter.builder().setSender(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("sender");

    assertThatThrownBy(
            () -> ZipkinSpanExporter.builder().setEncoder((zipkin2.codec.BytesEncoder<Span>) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("encoder");

    assertThatThrownBy(() -> ZipkinSpanExporter.builder().setEncoder((BytesEncoder<Span>) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("encoder");
  }

  @Test
  void encoderProtobuf() {
    @SuppressWarnings("deprecation") // we have to use the deprecated setEncoderto test it
    ZipkinSpanExporter exporter =
        ZipkinSpanExporter.builder().setEncoder(zipkin2.codec.SpanBytesEncoder.PROTO3).build();
    try {
      assertThat(exporter).extracting("encoder.encoding").isEqualTo(Encoding.PROTO3);
    } finally {
      exporter.shutdown();
    }

    exporter = ZipkinSpanExporter.builder().setEncoder(SpanBytesEncoder.PROTO3).build();
    try {
      assertThat(exporter).extracting("encoder").isEqualTo(SpanBytesEncoder.PROTO3);
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void compressionDefault() {
    ZipkinSpanExporter exporter = ZipkinSpanExporter.builder().build();
    try {
      assertThat(exporter).extracting("sender.delegate.compressionEnabled").isEqualTo(true);
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void compressionNone() {
    ZipkinSpanExporter exporter = ZipkinSpanExporter.builder().setCompression("none").build();
    try {
      assertThat(exporter).extracting("sender.delegate.compressionEnabled").isEqualTo(false);
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void compressionGzip() {
    ZipkinSpanExporter exporter = ZipkinSpanExporter.builder().setCompression("gzip").build();
    try {
      assertThat(exporter).extracting("sender.delegate.compressionEnabled").isEqualTo(true);
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void compressionEnabledAndDisabled() {
    ZipkinSpanExporter exporter =
        ZipkinSpanExporter.builder().setCompression("gzip").setCompression("none").build();
    try {
      assertThat(exporter).extracting("sender.delegate.compressionEnabled").isEqualTo(false);
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  @SuppressWarnings("PreferJavaTimeOverload")
  void readTimeout_Zero() {
    ZipkinSpanExporter exporter =
        ZipkinSpanExporter.builder().setReadTimeout(0, TimeUnit.SECONDS).build();

    try {
      assertThat(exporter)
          .extracting("sender.delegate.client.readTimeoutMillis")
          .isEqualTo(Integer.MAX_VALUE);
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void stringRepresentation() {
    try (ZipkinSpanExporter exporter = ZipkinSpanExporter.builder().build()) {
      assertThat(exporter.toString())
          .isEqualTo(
              "ZipkinSpanExporter{endpoint=http://localhost:9411/api/v2/spans, compressionEnabled=true, readTimeoutMillis=10000, internalTelemetrySchemaVersion=LEGACY}");
    }
    try (ZipkinSpanExporter exporter =
        ZipkinSpanExporter.builder()
            .setEndpoint("http://zipkin:9411/api/v2/spans")
            .setReadTimeout(Duration.ofSeconds(15))
            .setCompression("none")
            .build()) {
      assertThat(exporter.toString())
          .isEqualTo(
              "ZipkinSpanExporter{endpoint=http://zipkin:9411/api/v2/spans, compressionEnabled=false, readTimeoutMillis=15000, internalTelemetrySchemaVersion=LEGACY}");
    }
  }

  @Test
  void suppressInstrumentation() {
    TestSpanData testSpanData = spanBuilder().build();

    SuppressCatchingSender suppressCatchingSender = new SuppressCatchingSender(Encoding.JSON);
    ZipkinSpanExporter zipkinSpanExporter =
        new ZipkinSpanExporter(
            new ZipkinSpanExporterBuilder(),
            mockEncoder,
            suppressCatchingSender,
            MeterProvider::noop,
            InternalTelemetryVersion.LATEST,
            "http://testing:1234",
            mockTransformer);

    byte[] someBytes = new byte[0];
    Span zipkinSpan =
        zipkinSpanBuilder(Span.Kind.SERVER, localIp)
            .putTag(OtelToZipkinSpanTransformer.OTEL_STATUS_CODE, "OK")
            .build();
    when(mockTransformer.generateSpan(testSpanData)).thenReturn(zipkinSpan);
    when(mockEncoder.encode(zipkinSpan)).thenReturn(someBytes);

    zipkinSpanExporter.export(Collections.singleton(testSpanData));

    // Instrumentation should be suppressed on send, to avoid incidental spans related to span
    // export.
    assertTrue(suppressCatchingSender.sent.get());
    assertTrue(suppressCatchingSender.suppressed.get());
  }

  static class SuppressCatchingSender extends BytesMessageSender.Base {

    final AtomicBoolean sent = new AtomicBoolean();
    final AtomicBoolean suppressed = new AtomicBoolean();

    protected SuppressCatchingSender(Encoding encoding) {
      super(encoding);
    }

    @Override
    public int messageMaxBytes() {
      return 1024;
    }

    @Override
    public void send(List<byte[]> list) throws IOException {
      sent.set(true);
      suppressed.set(InstrumentationUtil.shouldSuppressInstrumentation(Context.current()));
    }

    @Override
    public void close() throws IOException {}
  }
}
