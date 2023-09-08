/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.jaeger.thrift;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import io.github.netmikey.logunit.api.LogCapturer;
import io.jaegertracing.internal.exceptions.SenderException;
import io.jaegertracing.thrift.internal.senders.ThriftSender;
import io.jaegertracing.thriftjava.Process;
import io.jaegertracing.thriftjava.Span;
import io.jaegertracing.thriftjava.SpanRef;
import io.jaegertracing.thriftjava.SpanRefType;
import io.jaegertracing.thriftjava.Tag;
import io.jaegertracing.thriftjava.TagType;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("deprecation") // Testing deprecated code
class JaegerThriftSpanExporterTest {

  private static final String TRACE_ID = "a0000000000000000000000000abc123";
  private static final long TRACE_ID_HIGH = 0xa000000000000000L;
  private static final long TRACE_ID_LOW = 0x0000000000abc123L;
  private static final String SPAN_ID = "00000f0000def456";
  private static final long SPAN_ID_LONG = 0x00000f0000def456L;
  private static final String SPAN_ID_2 = "00a0000000aef789";
  private static final long SPAN_ID_2_LONG = 0x00a0000000aef789L;
  private static final SpanContext SPAN_CONTEXT =
      SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault());
  private static final SpanContext SPAN_CONTEXT_2 =
      SpanContext.create(TRACE_ID, SPAN_ID_2, TraceFlags.getDefault(), TraceState.getDefault());
  private static final Duration DURATION = Duration.ofMillis(900);

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(JaegerThriftSpanExporter.class);

  private JaegerThriftSpanExporter exporter;
  @Mock private ThriftSender thriftSender;

  @BeforeEach
  void beforeEach() {
    exporter = JaegerThriftSpanExporter.builder().setThriftSender(thriftSender).build();
  }

  @Test
  void testExport() throws SenderException, UnknownHostException {
    SpanData span =
        testSpanData(
            Resource.create(
                Attributes.of(
                    stringKey("service.name"),
                    "myServiceName",
                    stringKey("resource-attr-key"),
                    "resource-attr-value")),
            "GET /api/endpoint",
            SPAN_CONTEXT,
            SPAN_CONTEXT_2);

    // test
    CompletableResultCode result = exporter.export(Collections.singletonList(span));
    result.join(1, TimeUnit.SECONDS);
    assertThat(result.isSuccess()).isEqualTo(true);

    // verify
    Process expectedProcess = new Process("myServiceName");
    expectedProcess.addToTags(
        new Tag("jaeger.version", TagType.STRING).setVStr("opentelemetry-java"));
    expectedProcess.addToTags(
        new Tag("ip", TagType.STRING).setVStr(InetAddress.getLocalHost().getHostAddress()));
    expectedProcess.addToTags(
        new Tag("hostname", TagType.STRING).setVStr(InetAddress.getLocalHost().getHostName()));
    expectedProcess.addToTags(
        new Tag("resource-attr-key", TagType.STRING).setVStr("resource-attr-value"));
    expectedProcess.addToTags(new Tag("service.name", TagType.STRING).setVStr("myServiceName"));

    Span expectedSpan =
        new Span()
            .setTraceIdHigh(TRACE_ID_HIGH)
            .setTraceIdLow(TRACE_ID_LOW)
            .setSpanId(SPAN_ID_LONG)
            .setOperationName("GET /api/endpoint")
            .setReferences(
                Collections.singletonList(
                    new SpanRef()
                        .setSpanId(SPAN_ID_2_LONG)
                        .setTraceIdHigh(TRACE_ID_HIGH)
                        .setTraceIdLow(TRACE_ID_LOW)
                        .setRefType(SpanRefType.CHILD_OF)))
            .setParentSpanId(SPAN_ID_2_LONG)
            .setStartTime(TimeUnit.NANOSECONDS.toMicros(span.getStartEpochNanos()))
            .setDuration(DURATION.toMillis() * 1000)
            .setLogs(Collections.emptyList());
    expectedSpan.addToTags(new Tag("span.kind", TagType.STRING).setVStr("consumer"));
    expectedSpan.addToTags(new Tag("otel.status_code", TagType.STRING).setVStr("OK"));
    expectedSpan.addToTags(
        new Tag("otel.scope.name", TagType.STRING).setVStr("io.opentelemetry.auto"));
    expectedSpan.addToTags(
        new Tag("otel.library.name", TagType.STRING).setVStr("io.opentelemetry.auto"));
    expectedSpan.addToTags(new Tag("otel.scope.version", TagType.STRING).setVStr("1.0.0"));
    expectedSpan.addToTags(new Tag("otel.library.version", TagType.STRING).setVStr("1.0.0"));

    List<Span> expectedSpans = Collections.singletonList(expectedSpan);
    verify(thriftSender).send(expectedProcess, expectedSpans);
  }

  @Test
  void testExportMultipleResources() throws SenderException, UnknownHostException {
    SpanData span =
        testSpanData(
            Resource.create(
                Attributes.of(
                    stringKey("service.name"),
                    "myServiceName1",
                    stringKey("resource-attr-key-1"),
                    "resource-attr-value-1")),
            "GET /api/endpoint/1",
            SPAN_CONTEXT,
            SpanContext.getInvalid());

    SpanData span2 =
        testSpanData(
            Resource.create(
                Attributes.of(
                    stringKey("service.name"),
                    "myServiceName2",
                    stringKey("resource-attr-key-2"),
                    "resource-attr-value-2")),
            "GET /api/endpoint/2",
            SPAN_CONTEXT_2,
            SpanContext.getInvalid());

    // test
    CompletableResultCode result = exporter.export(Arrays.asList(span, span2));
    result.join(1, TimeUnit.SECONDS);
    assertThat(result.isSuccess()).isEqualTo(true);

    // verify
    Process expectedProcess1 = new Process("myServiceName1");
    expectedProcess1.addToTags(
        new Tag("jaeger.version", TagType.STRING).setVStr("opentelemetry-java"));
    expectedProcess1.addToTags(
        new Tag("ip", TagType.STRING).setVStr(InetAddress.getLocalHost().getHostAddress()));
    expectedProcess1.addToTags(
        new Tag("hostname", TagType.STRING).setVStr(InetAddress.getLocalHost().getHostName()));
    expectedProcess1.addToTags(
        new Tag("resource-attr-key-1", TagType.STRING).setVStr("resource-attr-value-1"));
    expectedProcess1.addToTags(new Tag("service.name", TagType.STRING).setVStr("myServiceName1"));

    Process expectedProcess2 = new Process("myServiceName2");
    expectedProcess2.addToTags(
        new Tag("jaeger.version", TagType.STRING).setVStr("opentelemetry-java"));
    expectedProcess2.addToTags(
        new Tag("ip", TagType.STRING).setVStr(InetAddress.getLocalHost().getHostAddress()));
    expectedProcess2.addToTags(
        new Tag("hostname", TagType.STRING).setVStr(InetAddress.getLocalHost().getHostName()));
    expectedProcess2.addToTags(
        new Tag("resource-attr-key-2", TagType.STRING).setVStr("resource-attr-value-2"));
    expectedProcess2.addToTags(new Tag("service.name", TagType.STRING).setVStr("myServiceName2"));

    Span expectedSpan1 =
        new Span()
            .setTraceIdHigh(TRACE_ID_HIGH)
            .setTraceIdLow(TRACE_ID_LOW)
            .setSpanId(SPAN_ID_LONG)
            .setOperationName("GET /api/endpoint/1")
            .setReferences(Collections.emptyList())
            .setStartTime(TimeUnit.NANOSECONDS.toMicros(span.getStartEpochNanos()))
            .setDuration(DURATION.toMillis() * 1000)
            .setLogs(Collections.emptyList());
    expectedSpan1.addToTags(new Tag("span.kind", TagType.STRING).setVStr("consumer"));
    expectedSpan1.addToTags(new Tag("otel.status_code", TagType.STRING).setVStr("OK"));
    expectedSpan1.addToTags(
        new Tag("otel.scope.name", TagType.STRING).setVStr("io.opentelemetry.auto"));
    expectedSpan1.addToTags(
        new Tag("otel.library.name", TagType.STRING).setVStr("io.opentelemetry.auto"));
    expectedSpan1.addToTags(new Tag("otel.scope.version", TagType.STRING).setVStr("1.0.0"));
    expectedSpan1.addToTags(new Tag("otel.library.version", TagType.STRING).setVStr("1.0.0"));

    Span expectedSpan2 =
        new Span()
            .setTraceIdHigh(TRACE_ID_HIGH)
            .setTraceIdLow(TRACE_ID_LOW)
            .setSpanId(SPAN_ID_2_LONG)
            .setOperationName("GET /api/endpoint/2")
            .setReferences(Collections.emptyList())
            .setStartTime(TimeUnit.NANOSECONDS.toMicros(span2.getStartEpochNanos()))
            .setDuration(DURATION.toMillis() * 1000)
            .setLogs(Collections.emptyList());
    expectedSpan2.addToTags(new Tag("span.kind", TagType.STRING).setVStr("consumer"));
    expectedSpan2.addToTags(new Tag("otel.status_code", TagType.STRING).setVStr("OK"));
    expectedSpan2.addToTags(
        new Tag("otel.scope.name", TagType.STRING).setVStr("io.opentelemetry.auto"));
    expectedSpan2.addToTags(
        new Tag("otel.library.name", TagType.STRING).setVStr("io.opentelemetry.auto"));
    expectedSpan2.addToTags(new Tag("otel.scope.version", TagType.STRING).setVStr("1.0.0"));
    expectedSpan2.addToTags(new Tag("otel.library.version", TagType.STRING).setVStr("1.0.0"));

    verify(thriftSender).send(expectedProcess2, Collections.singletonList(expectedSpan2));
    verify(thriftSender).send(expectedProcess1, Collections.singletonList(expectedSpan1));
  }

  @Test
  @SuppressLogger(JaegerThriftSpanExporter.class)
  void shutdown() {
    assertThat(exporter.shutdown().join(1, TimeUnit.SECONDS).isSuccess()).isTrue();
    assertThat(logs.getEvents()).isEmpty();
    assertThat(
            exporter
                .export(
                    Collections.singletonList(
                        testSpanData(
                            Resource.getDefault(),
                            "span name",
                            SPAN_CONTEXT,
                            SpanContext.getInvalid())))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isFalse();
    assertThat(exporter.shutdown().join(1, TimeUnit.SECONDS).isSuccess()).isTrue();
    logs.assertContains("Calling shutdown() multiple times.");
  }

  private static SpanData testSpanData(
      Resource resource, String spanName, SpanContext spanContext, SpanContext parentContext) {
    long startMs = System.currentTimeMillis();
    long endMs = startMs + DURATION.toMillis();
    return TestSpanData.builder()
        .setHasEnded(true)
        .setSpanContext(spanContext)
        .setParentSpanContext(parentContext)
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
}
