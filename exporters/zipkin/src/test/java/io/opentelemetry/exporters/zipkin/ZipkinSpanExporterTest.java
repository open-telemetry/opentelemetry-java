/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.exporters.zipkin;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Link;
import io.opentelemetry.sdk.trace.export.SpanExporter.ResultCode;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import zipkin2.Call;
import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.reporter.Sender;

/** Unit tests for {@link ZipkinSpanExporterTest}. */
@RunWith(MockitoJUnitRunner.class)
public class ZipkinSpanExporterTest {

  @Mock private Sender mockSender;
  @Mock private SpanBytesEncoder mockEncoder;
  @Mock private Call<Void> mockZipkinCall;

  private static final Endpoint localEndpoint =
      ZipkinSpanExporter.produceLocalEndpoint("tweetiebird");
  private static final String TRACE_ID = "d239036e7d5cec116b562147388b35bf";
  private static final String SPAN_ID = "9cc1e3049173be09";
  private static final String PARENT_SPAN_ID = "8b03ab423da481c5";
  private static final Map<String, AttributeValue> attributes = Collections.emptyMap();
  private static final List<SpanData.TimedEvent> annotations =
      ImmutableList.of(
          SpanData.TimedEvent.create(
              1505855799_433901068L, "RECEIVED", Collections.<String, AttributeValue>emptyMap()),
          SpanData.TimedEvent.create(
              1505855799_459486280L, "SENT", Collections.<String, AttributeValue>emptyMap()));

  @Test
  public void generateSpan_remoteParent() {
    SpanData data = buildStandardSpan().build();

    assertThat(ZipkinSpanExporter.generateSpan(data, localEndpoint))
        .isEqualTo(buildZipkinSpan(Span.Kind.SERVER, "OK"));
  }

  @Test
  public void generateSpan_ServerKind() {
    SpanData data =
        buildStandardSpan()
            .setParentSpanId(SpanId.fromLowerBase16(PARENT_SPAN_ID, 0))
            .setKind(Kind.SERVER)
            .build();

    assertThat(ZipkinSpanExporter.generateSpan(data, localEndpoint))
        .isEqualTo(buildZipkinSpan(Span.Kind.SERVER, "OK"));
  }

  @Test
  public void generateSpan_ClientKind() {
    SpanData data =
        buildStandardSpan()
            .setParentSpanId(SpanId.fromLowerBase16(PARENT_SPAN_ID, 0))
            .setKind(Kind.CLIENT)
            .build();

    assertThat(ZipkinSpanExporter.generateSpan(data, localEndpoint))
        .isEqualTo(buildZipkinSpan(Span.Kind.CLIENT, "OK"));
  }

  @Test
  public void generateSpan_WithAttributes() {
    Map<String, AttributeValue> attributeMap = new HashMap<>();
    attributeMap.put("string", AttributeValue.stringAttributeValue("string value"));
    attributeMap.put("boolean", AttributeValue.booleanAttributeValue(false));
    attributeMap.put("long", AttributeValue.longAttributeValue(9999L));
    attributeMap.put("double", AttributeValue.doubleAttributeValue(222.333));
    SpanData data =
        buildStandardSpan()
            .setAttributes(attributeMap)
            .setParentSpanId(SpanId.fromLowerBase16(PARENT_SPAN_ID, 0))
            .setKind(Kind.CLIENT)
            .setStatus(Status.OK)
            .build();

    assertThat(ZipkinSpanExporter.generateSpan(data, localEndpoint))
        .isEqualTo(
            buildZipkinSpan(Span.Kind.CLIENT, "OK")
                .toBuilder()
                .putTag("string", "string value")
                .putTag("boolean", "false")
                .putTag("long", "9999")
                .putTag("double", "222.333")
                .build());
  }

  @Test
  public void generateSpan_WithErrorStatus() {
    String errorMessage = "timeout";

    SpanData data =
        buildStandardSpan()
            .setStatus(Status.DEADLINE_EXCEEDED.withDescription(errorMessage))
            .setKind(Kind.SERVER)
            .setParentSpanId(SpanId.fromLowerBase16(PARENT_SPAN_ID, 0))
            .build();

    assertThat(ZipkinSpanExporter.generateSpan(data, localEndpoint))
        .isEqualTo(
            buildZipkinSpan(Span.Kind.SERVER, "DEADLINE_EXCEEDED")
                .toBuilder()
                .putTag(ZipkinSpanExporter.STATUS_DESCRIPTION, errorMessage)
                .putTag(ZipkinSpanExporter.STATUS_ERROR, "DEADLINE_EXCEEDED")
                .build());
  }

  @Test
  public void testExport() throws IOException {
    ZipkinSpanExporter zipkinSpanExporter =
        new ZipkinSpanExporter(mockEncoder, mockSender, "tweetiebird");

    byte[] someBytes = new byte[0];
    when(mockEncoder.encode(buildZipkinSpan(Span.Kind.SERVER, "OK"))).thenReturn(someBytes);
    when(mockSender.sendSpans(Collections.singletonList(someBytes))).thenReturn(mockZipkinCall);
    ResultCode resultCode =
        zipkinSpanExporter.export(Collections.singleton(buildStandardSpan().build()));

    verify(mockZipkinCall).execute();
    assertThat(resultCode).isEqualTo(ResultCode.SUCCESS);
  }

  @Test
  public void testExport_failed() throws IOException {
    ZipkinSpanExporter zipkinSpanExporter =
        new ZipkinSpanExporter(mockEncoder, mockSender, "tweetiebird");

    byte[] someBytes = new byte[0];
    when(mockEncoder.encode(buildZipkinSpan(Span.Kind.SERVER, "OK"))).thenReturn(someBytes);
    when(mockSender.sendSpans(Collections.singletonList(someBytes))).thenReturn(mockZipkinCall);
    when(mockZipkinCall.execute()).thenThrow(new IOException());

    ResultCode resultCode =
        zipkinSpanExporter.export(Collections.singleton(buildStandardSpan().build()));

    assertThat(resultCode).isEqualTo(ResultCode.FAILED_NOT_RETRYABLE);
  }

  @Test
  public void testCreate() {
    ZipkinExporterConfiguration configuration =
        ZipkinExporterConfiguration.builder()
            .setV2Url("https://zipkin.endpoint.com/v2")
            .setServiceName("myGreatService")
            .build();

    ZipkinSpanExporter exporter = ZipkinSpanExporter.create(configuration);
    assertThat(exporter).isNotNull();
  }

  private static SpanData.Builder buildStandardSpan() {
    return SpanData.newBuilder()
        .setTraceId(TraceId.fromLowerBase16(TRACE_ID, 0))
        .setSpanId(SpanId.fromLowerBase16(SPAN_ID, 0))
        .setParentSpanId(SpanId.fromLowerBase16(PARENT_SPAN_ID, 0))
        .setTraceFlags(TraceFlags.builder().setIsSampled(true).build())
        .setStatus(Status.OK)
        .setKind(Kind.SERVER)
        .setHasRemoteParent(true)
        .setName("Recv.helloworld.Greeter.SayHello")
        .setStartEpochNanos(1505855794_194009601L)
        .setAttributes(attributes)
        .setTotalAttributeCount(attributes.size())
        .setTimedEvents(annotations)
        .setLinks(Collections.<Link>emptyList())
        .setEndEpochNanos(1505855799_465726528L)
        .setHasEnded(true);
  }

  private static Span buildZipkinSpan(Span.Kind kind, String status) {
    return Span.newBuilder()
        .traceId(TRACE_ID)
        .parentId(PARENT_SPAN_ID)
        .id(SPAN_ID)
        .kind(kind)
        .name("Recv.helloworld.Greeter.SayHello")
        .timestamp(1505855794000000L + 194009601L / 1000)
        .duration((1505855799000000L + 465726528L / 1000) - (1505855794000000L + 194009601L / 1000))
        .localEndpoint(localEndpoint)
        .addAnnotation(1505855799000000L + 433901068L / 1000, "RECEIVED")
        .addAnnotation(1505855799000000L + 459486280L / 1000, "SENT")
        .putTag(ZipkinSpanExporter.STATUS_CODE, status)
        .build();
  }
}
