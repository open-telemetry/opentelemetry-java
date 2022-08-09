/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin;

import static io.opentelemetry.exporter.zipkin.ZipkinTestSpan.buildStandardSpan;
import static io.opentelemetry.exporter.zipkin.ZipkinTestSpan.standardZipkinSpanBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zipkin2.Call;
import zipkin2.Callback;
import zipkin2.Span;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.reporter.Sender;

@ExtendWith(MockitoExtension.class)
class ZipkinSpanExporterTest {

  @Mock private Sender mockSender;
  @Mock private SpanBytesEncoder mockEncoder;
  @Mock private Call<Void> mockZipkinCall;
  @Mock private Function<SpanData, Span> mockTransformer;

  @Test
  void testExport() {
    TestSpanData testSpanData = buildStandardSpan().build();

    ZipkinSpanExporter zipkinSpanExporter =
        new ZipkinSpanExporter(mockEncoder, mockSender, MeterProvider.noop(), mockTransformer);

    byte[] someBytes = new byte[0];
    Span zipkinSpan =
        standardZipkinSpanBuilder(Span.Kind.SERVER)
            .putTag(OtelToZipkinSpanTransformer.OTEL_STATUS_CODE, "OK")
            .build();
    when(mockTransformer.apply(testSpanData)).thenReturn(zipkinSpan);
    when(mockEncoder.encode(zipkinSpan)).thenReturn(someBytes);
    when(mockSender.sendSpans(Collections.singletonList(someBytes))).thenReturn(mockZipkinCall);
    doAnswer(
            invocation -> {
              Callback<Void> callback = invocation.getArgument(0);
              callback.onSuccess(null);
              return null;
            })
        .when(mockZipkinCall)
        .enqueue(any());

    CompletableResultCode resultCode =
        zipkinSpanExporter.export(Collections.singleton(testSpanData));

    assertThat(resultCode.isSuccess()).isTrue();
  }

  @Test
  @SuppressLogger(ZipkinSpanExporter.class)
  void testExport_failed() {
    TestSpanData testSpanData = buildStandardSpan().build();
    ZipkinSpanExporter zipkinSpanExporter =
        new ZipkinSpanExporter(mockEncoder, mockSender, MeterProvider.noop(), mockTransformer);

    byte[] someBytes = new byte[0];
    Span zipkinSpan =
        standardZipkinSpanBuilder(Span.Kind.SERVER)
            .putTag(OtelToZipkinSpanTransformer.OTEL_STATUS_CODE, "OK")
            .build();
    when(mockTransformer.apply(testSpanData)).thenReturn(zipkinSpan);
    when(mockEncoder.encode(zipkinSpan)).thenReturn(someBytes);
    when(mockSender.sendSpans(Collections.singletonList(someBytes))).thenReturn(mockZipkinCall);
    doAnswer(
            invocation -> {
              Callback<Void> callback = invocation.getArgument(0);
              callback.onError(new IOException());
              return null;
            })
        .when(mockZipkinCall)
        .enqueue(any());

    CompletableResultCode resultCode =
        zipkinSpanExporter.export(Collections.singleton(testSpanData));

    assertThat(resultCode.isSuccess()).isFalse();
  }

  @Test
  void testCreate() {
    ZipkinSpanExporter exporter = ZipkinSpanExporter.builder().setSender(mockSender).build();

    assertThat(exporter).isNotNull();
  }

  @Test
  void testShutdown() throws IOException {
    ZipkinSpanExporter exporter = ZipkinSpanExporter.builder().setSender(mockSender).build();

    exporter.shutdown();
    verify(mockSender).close();
  }

  @Test
  @SuppressWarnings("PreferJavaTimeOverload")
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

    assertThatThrownBy(() -> ZipkinSpanExporter.builder().setSender(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("sender");

    assertThatThrownBy(() -> ZipkinSpanExporter.builder().setEncoder(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("encoder");
  }
}
