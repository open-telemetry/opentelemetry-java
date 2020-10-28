/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporters.jaeger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Lists;
import io.jaegertracing.internal.exceptions.SenderException;
import io.jaegertracing.thrift.internal.senders.ThriftSender;
import io.jaegertracing.thriftjava.Process;
import io.jaegertracing.thriftjava.Span;
import io.jaegertracing.thriftjava.Tag;
import io.jaegertracing.thriftjava.TagType;
import io.opentelemetry.common.AttributeKey;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Status;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class JaegerThriftSpanExporterTest {

  private static final String TRACE_ID = "a0000000000000000000000000abc123";
  private static final String SPAN_ID = "00000f0000def456";
  private static final String SPAN_ID_2 = "00a0000000aef789";

  private JaegerThriftSpanExporter exporter;
  @Mock private ThriftSender thriftSender;

  @BeforeEach
  void beforeEach() {
    MockitoAnnotations.initMocks(this);
    exporter =
        JaegerThriftSpanExporter.builder()
            .setThriftSender(thriftSender)
            .setServiceName("myservice.name")
            .build();
  }

  @Test
  void testExport() throws SenderException, UnknownHostException {
    long duration = 900; // ms
    long startMs = System.currentTimeMillis();
    long endMs = startMs + duration;
    SpanData span =
        TestSpanData.builder()
            .setHasEnded(true)
            .setTraceId(TRACE_ID)
            .setSpanId(SPAN_ID)
            .setName("GET /api/endpoint")
            .setStartEpochNanos(TimeUnit.MILLISECONDS.toNanos(startMs))
            .setEndEpochNanos(TimeUnit.MILLISECONDS.toNanos(endMs))
            .setStatus(Status.ok())
            .setKind(Kind.CONSUMER)
            .setLinks(Collections.emptyList())
            .setTotalRecordedLinks(0)
            .setTotalRecordedEvents(0)
            .setInstrumentationLibraryInfo(
                InstrumentationLibraryInfo.create("io.opentelemetry.auto", "1.0.0"))
            .setResource(
                Resource.create(
                    Attributes.of(
                        AttributeKey.stringKey("resource-attr-key"), "resource-attr-value")))
            .build();

    // test
    CompletableResultCode result = exporter.export(Collections.singletonList(span));
    result.join(1, TimeUnit.SECONDS);
    assertThat(result.isSuccess()).isEqualTo(true);

    // verify
    Process expectedProcess = new Process("myservice.name");
    expectedProcess.addToTags(
        new Tag("jaeger.version", TagType.STRING).setVStr("opentelemetry-java"));
    expectedProcess.addToTags(
        new Tag("ip", TagType.STRING).setVStr(InetAddress.getLocalHost().getHostAddress()));
    expectedProcess.addToTags(
        new Tag("hostname", TagType.STRING).setVStr(InetAddress.getLocalHost().getHostName()));
    expectedProcess.addToTags(
        new Tag("resource-attr-key", TagType.STRING).setVStr("resource-attr-value"));

    Span expectedSpan =
        new Span()
            .setTraceIdHigh(TraceId.traceIdHighBytesAsLong(TRACE_ID))
            .setTraceIdLow(TraceId.traceIdLowBytesAsLong(TRACE_ID))
            .setSpanId(SpanId.asLong(SPAN_ID))
            .setOperationName("GET /api/endpoint")
            .setReferences(Collections.emptyList())
            .setStartTime(TimeUnit.MILLISECONDS.toMicros(startMs))
            .setDuration(TimeUnit.MILLISECONDS.toMicros(duration))
            .setLogs(Collections.emptyList());
    expectedSpan.addToTags(new Tag("span.kind", TagType.STRING).setVStr("consumer"));
    expectedSpan.addToTags(new Tag("span.status.code", TagType.LONG).setVLong(0));
    expectedSpan.addToTags(
        new Tag("otel.library.name", TagType.STRING).setVStr("io.opentelemetry.auto"));
    expectedSpan.addToTags(new Tag("otel.library.version", TagType.STRING).setVStr("1.0.0"));

    List<Span> expectedSpans = Collections.singletonList(expectedSpan);
    verify(thriftSender).send(expectedProcess, expectedSpans);
  }

  @Test
  void testExportMultipleResources() throws SenderException, UnknownHostException {
    long duration = 900; // ms
    long startMs = System.currentTimeMillis();
    long endMs = startMs + duration;
    SpanData span =
        TestSpanData.builder()
            .setHasEnded(true)
            .setTraceId(TRACE_ID)
            .setSpanId(SPAN_ID)
            .setName("GET /api/endpoint/1")
            .setStartEpochNanos(TimeUnit.MILLISECONDS.toNanos(startMs))
            .setEndEpochNanos(TimeUnit.MILLISECONDS.toNanos(endMs))
            .setStatus(Status.ok())
            .setKind(Kind.CONSUMER)
            .setLinks(Collections.emptyList())
            .setTotalRecordedLinks(0)
            .setTotalRecordedEvents(0)
            .setInstrumentationLibraryInfo(
                InstrumentationLibraryInfo.create("io.opentelemetry.auto", "1.0.0"))
            .setResource(
                Resource.create(
                    Attributes.of(
                        AttributeKey.stringKey("resource-attr-key-1"), "resource-attr-value-1")))
            .build();

    SpanData span2 =
        TestSpanData.builder()
            .setHasEnded(true)
            .setTraceId(TRACE_ID)
            .setSpanId(SPAN_ID_2)
            .setName("GET /api/endpoint/2")
            .setStartEpochNanos(TimeUnit.MILLISECONDS.toNanos(startMs))
            .setEndEpochNanos(TimeUnit.MILLISECONDS.toNanos(endMs))
            .setStatus(Status.ok())
            .setKind(Kind.CONSUMER)
            .setLinks(Collections.emptyList())
            .setTotalRecordedLinks(0)
            .setTotalRecordedEvents(0)
            .setInstrumentationLibraryInfo(
                InstrumentationLibraryInfo.create("io.opentelemetry.auto", "1.0.0"))
            .setResource(
                Resource.create(
                    Attributes.of(
                        AttributeKey.stringKey("resource-attr-key-2"), "resource-attr-value-2")))
            .build();

    // test
    CompletableResultCode result = exporter.export(Lists.newArrayList(span, span2));
    result.join(1, TimeUnit.SECONDS);
    assertThat(result.isSuccess()).isEqualTo(true);

    // verify
    Process expectedProcess1 = new Process("myservice.name");
    expectedProcess1.addToTags(
        new Tag("jaeger.version", TagType.STRING).setVStr("opentelemetry-java"));
    expectedProcess1.addToTags(
        new Tag("ip", TagType.STRING).setVStr(InetAddress.getLocalHost().getHostAddress()));
    expectedProcess1.addToTags(
        new Tag("hostname", TagType.STRING).setVStr(InetAddress.getLocalHost().getHostName()));
    expectedProcess1.addToTags(
        new Tag("resource-attr-key-1", TagType.STRING).setVStr("resource-attr-value-1"));

    Process expectedProcess2 = new Process("myservice.name");
    expectedProcess2.addToTags(
        new Tag("jaeger.version", TagType.STRING).setVStr("opentelemetry-java"));
    expectedProcess2.addToTags(
        new Tag("ip", TagType.STRING).setVStr(InetAddress.getLocalHost().getHostAddress()));
    expectedProcess2.addToTags(
        new Tag("hostname", TagType.STRING).setVStr(InetAddress.getLocalHost().getHostName()));
    expectedProcess2.addToTags(
        new Tag("resource-attr-key-2", TagType.STRING).setVStr("resource-attr-value-2"));

    Span expectedSpan1 =
        new Span()
            .setTraceIdHigh(TraceId.traceIdHighBytesAsLong(TRACE_ID))
            .setTraceIdLow(TraceId.traceIdLowBytesAsLong(TRACE_ID))
            .setSpanId(SpanId.asLong(SPAN_ID))
            .setOperationName("GET /api/endpoint/1")
            .setReferences(Collections.emptyList())
            .setStartTime(TimeUnit.MILLISECONDS.toMicros(startMs))
            .setDuration(TimeUnit.MILLISECONDS.toMicros(duration))
            .setLogs(Collections.emptyList());
    expectedSpan1.addToTags(new Tag("span.kind", TagType.STRING).setVStr("consumer"));
    expectedSpan1.addToTags(new Tag("span.status.code", TagType.LONG).setVLong(0));
    expectedSpan1.addToTags(
        new Tag("otel.library.name", TagType.STRING).setVStr("io.opentelemetry.auto"));
    expectedSpan1.addToTags(new Tag("otel.library.version", TagType.STRING).setVStr("1.0.0"));

    Span expectedSpan2 =
        new Span()
            .setTraceIdHigh(TraceId.traceIdHighBytesAsLong(TRACE_ID))
            .setTraceIdLow(TraceId.traceIdLowBytesAsLong(TRACE_ID))
            .setSpanId(SpanId.asLong(SPAN_ID_2))
            .setOperationName("GET /api/endpoint/2")
            .setReferences(Collections.emptyList())
            .setStartTime(TimeUnit.MILLISECONDS.toMicros(startMs))
            .setDuration(TimeUnit.MILLISECONDS.toMicros(duration))
            .setLogs(Collections.emptyList());
    expectedSpan2.addToTags(new Tag("span.kind", TagType.STRING).setVStr("consumer"));
    expectedSpan2.addToTags(new Tag("span.status.code", TagType.LONG).setVLong(0));
    expectedSpan2.addToTags(
        new Tag("otel.library.name", TagType.STRING).setVStr("io.opentelemetry.auto"));
    expectedSpan2.addToTags(new Tag("otel.library.version", TagType.STRING).setVStr("1.0.0"));

    verify(thriftSender).send(expectedProcess2, Collections.singletonList(expectedSpan2));
    verify(thriftSender).send(expectedProcess1, Collections.singletonList(expectedSpan1));
  }

  @Test
  void configTest() {
    Map<String, String> options = new HashMap<>();
    String serviceName = "myGreatService";
    String endpoint = "http://127.0.0.1:9090";
    options.put("otel.exporter.jaeger.service.name", serviceName);
    options.put("otel.exporter.jaeger.endpoint", endpoint);
    JaegerThriftSpanExporter.Builder config = JaegerThriftSpanExporter.builder();
    JaegerThriftSpanExporter.Builder spy = Mockito.spy(config);
    spy.fromConfigMap(options, ConfigBuilderTest.getNaming()).build();
    verify(spy).setServiceName(serviceName);
    verify(spy).setEndpoint(endpoint);
  }

  abstract static class ConfigBuilderTest extends ConfigBuilder<ConfigBuilderTest> {

    public static NamingConvention getNaming() {
      return NamingConvention.DOT;
    }
  }
}
