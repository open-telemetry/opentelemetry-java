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
import static io.opentelemetry.common.AttributeValue.booleanAttributeValue;
import static io.opentelemetry.common.AttributeValue.doubleAttributeValue;
import static io.opentelemetry.common.AttributeValue.longAttributeValue;
import static io.opentelemetry.common.AttributeValue.stringAttributeValue;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Link;
import io.opentelemetry.sdk.trace.export.SpanExporter.ResultCode;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.attributes.SemanticAttributes;
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

    assertThat(ZipkinSpanExporter.generateSpan(data)).isEqualTo(buildZipkinSpan(Span.Kind.SERVER));
  }

  @Test
  public void generateSpan_ServerKind() {
    SpanData data = buildStandardSpan().setKind(Kind.SERVER).build();

    assertThat(ZipkinSpanExporter.generateSpan(data)).isEqualTo(buildZipkinSpan(Span.Kind.SERVER));
  }

  @Test
  public void generateSpan_ClientKind() {
    SpanData data = buildStandardSpan().setKind(Kind.CLIENT).build();

    assertThat(ZipkinSpanExporter.generateSpan(data)).isEqualTo(buildZipkinSpan(Span.Kind.CLIENT));
  }

  @Test
  public void generateSpan_InternalKind() {
    SpanData data = buildStandardSpan().setKind(Kind.INTERNAL).build();

    assertThat(ZipkinSpanExporter.generateSpan(data)).isEqualTo(buildZipkinSpan(null));
  }

  @Test
  public void generateSpan_ConsumeKind() {
    SpanData data = buildStandardSpan().setKind(Kind.CONSUMER).build();

    assertThat(ZipkinSpanExporter.generateSpan(data))
        .isEqualTo(buildZipkinSpan(Span.Kind.CONSUMER));
  }

  @Test
  public void generateSpan_ProducerKind() {
    SpanData data = buildStandardSpan().setKind(Kind.PRODUCER).build();

    assertThat(ZipkinSpanExporter.generateSpan(data))
        .isEqualTo(buildZipkinSpan(Span.Kind.PRODUCER));
  }

  @Test
  public void generateSpan_ResourceServiceNameMapping() {
    final Resource resource =
        Resource.create(
            singletonMap("service.name", stringAttributeValue("super-zipkin-service")));
    SpanData data = buildStandardSpan().setResource(resource).build();

    Endpoint expectedEndpoint = Endpoint.newBuilder().serviceName("super-zipkin-service").build();
    Span expectedZipkinSpan =
        buildZipkinSpan(Span.Kind.SERVER).toBuilder().localEndpoint(expectedEndpoint).build();
    assertThat(ZipkinSpanExporter.generateSpan(data)).isEqualTo(expectedZipkinSpan);
  }

  @Test
  public void generateSpan_WithAttributes() {
    Map<String, AttributeValue> attributeMap = new HashMap<>();
    attributeMap.put("string", stringAttributeValue("string value"));
    attributeMap.put("boolean", booleanAttributeValue(false));
    attributeMap.put("long", longAttributeValue(9999L));
    attributeMap.put("double", doubleAttributeValue(222.333));
    SpanData data = buildStandardSpan().setAttributes(attributeMap).setKind(Kind.CLIENT).build();

    assertThat(ZipkinSpanExporter.generateSpan(data))
        .isEqualTo(
            buildZipkinSpan(Span.Kind.CLIENT)
                .toBuilder()
                .putTag("string", "string value")
                .putTag("boolean", "false")
                .putTag("long", "9999")
                .putTag("double", "222.333")
                .build());
  }

  @Test
  public void generateSpan_AlreadyHasHttpStatusInfo() {
    Map<String, AttributeValue> attributeMap = new HashMap<>();
    attributeMap.put(
        SemanticAttributes.HTTP_STATUS_CODE.key(), longAttributeValue(404));
    attributeMap.put(
        SemanticAttributes.HTTP_STATUS_TEXT.key(),
        stringAttributeValue("NOT FOUND"));
    attributeMap.put("error", stringAttributeValue("A user provided error"));
    SpanData data =
        buildStandardSpan()
            .setAttributes(attributeMap)
            .setKind(Kind.CLIENT)
            .setStatus(Status.NOT_FOUND)
            .build();

    assertThat(ZipkinSpanExporter.generateSpan(data))
        .isEqualTo(
            buildZipkinSpan(Span.Kind.CLIENT)
                .toBuilder()
                .clearTags()
                .putTag(SemanticAttributes.HTTP_STATUS_CODE.key(), "404")
                .putTag(SemanticAttributes.HTTP_STATUS_TEXT.key(), "NOT FOUND")
                .putTag("error", "A user provided error")
                .build());
  }

  @Test
  public void generateSpan_WithRpcErrorStatus() {
    Map<String, AttributeValue> attributeMap = new HashMap<>();
    attributeMap.put(
        SemanticAttributes.RPC_SERVICE.key(),
        stringAttributeValue("my service name"));

    String errorMessage = "timeout";

    SpanData data =
        buildStandardSpan()
            .setStatus(Status.DEADLINE_EXCEEDED.withDescription(errorMessage))
            .setAttributes(attributeMap)
            .build();

    assertThat(ZipkinSpanExporter.generateSpan(data))
        .isEqualTo(
            buildZipkinSpan(Span.Kind.SERVER)
                .toBuilder()
                .putTag(ZipkinSpanExporter.GRPC_STATUS_DESCRIPTION, errorMessage)
                .putTag(SemanticAttributes.RPC_SERVICE.key(), "my service name")
                .putTag(ZipkinSpanExporter.GRPC_STATUS_CODE, "DEADLINE_EXCEEDED")
                .putTag(ZipkinSpanExporter.STATUS_ERROR, "DEADLINE_EXCEEDED")
                .build());
  }

  @Test
  public void testExport() throws IOException {
    ZipkinSpanExporter zipkinSpanExporter = new ZipkinSpanExporter(mockEncoder, mockSender);

    byte[] someBytes = new byte[0];
    when(mockEncoder.encode(buildZipkinSpan(Span.Kind.SERVER))).thenReturn(someBytes);
    when(mockSender.sendSpans(Collections.singletonList(someBytes))).thenReturn(mockZipkinCall);
    ResultCode resultCode =
        zipkinSpanExporter.export(Collections.singleton(buildStandardSpan().build()));

    verify(mockZipkinCall).execute();
    assertThat(resultCode).isEqualTo(ResultCode.SUCCESS);
  }

  @Test
  public void testExport_failed() throws IOException {
    ZipkinSpanExporter zipkinSpanExporter = new ZipkinSpanExporter(mockEncoder, mockSender);

    byte[] someBytes = new byte[0];
    when(mockEncoder.encode(buildZipkinSpan(Span.Kind.SERVER))).thenReturn(someBytes);
    when(mockSender.sendSpans(Collections.singletonList(someBytes))).thenReturn(mockZipkinCall);
    when(mockZipkinCall.execute()).thenThrow(new IOException());

    ResultCode resultCode =
        zipkinSpanExporter.export(Collections.singleton(buildStandardSpan().build()));

    assertThat(resultCode).isEqualTo(ResultCode.FAILED_NOT_RETRYABLE);
  }

  @Test
  public void testCreate() {
    ZipkinExporterConfiguration configuration =
        ZipkinExporterConfiguration.builder().setSender(mockSender).build();

    ZipkinSpanExporter exporter = ZipkinSpanExporter.create(configuration);
    assertThat(exporter).isNotNull();
  }

  @Test
  public void testShutdown() throws IOException {
    ZipkinExporterConfiguration configuration =
        ZipkinExporterConfiguration.builder().setSender(mockSender).build();

    ZipkinSpanExporter exporter = ZipkinSpanExporter.create(configuration);
    exporter.shutdown();
    verify(mockSender).close();
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

  private static Span buildZipkinSpan(Span.Kind kind) {
    return Span.newBuilder()
        .traceId(TRACE_ID)
        .parentId(PARENT_SPAN_ID)
        .id(SPAN_ID)
        .kind(kind)
        .name("Recv.helloworld.Greeter.SayHello")
        .timestamp(1505855794000000L + 194009601L / 1000)
        .duration((1505855799000000L + 465726528L / 1000) - (1505855794000000L + 194009601L / 1000))
        .addAnnotation(1505855799000000L + 433901068L / 1000, "RECEIVED")
        .addAnnotation(1505855799000000L + 459486280L / 1000, "SENT")
        //        .putTag(ZipkinSpanExporter.STATUS_CODE, status)
        .build();
  }
}
