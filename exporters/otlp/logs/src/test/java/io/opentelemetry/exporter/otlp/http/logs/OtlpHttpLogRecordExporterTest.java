/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.rpc.Status;
import com.linecorp.armeria.common.AggregatedHttpRequest;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.testing.junit5.server.mock.MockWebServerExtension;
import com.linecorp.armeria.testing.junit5.server.mock.RecordedRequest;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.exporter.internal.okhttp.OkHttpExporter;
import io.opentelemetry.exporter.internal.otlp.logs.ResourceLogsMarshaler;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import io.opentelemetry.exporter.internal.retry.RetryUtil;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.logs.TestLogRecordData;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import okhttp3.tls.HeldCertificate;
import okio.Buffer;
import okio.GzipSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

class OtlpHttpLogRecordExporterTest {

  private static final MediaType APPLICATION_PROTOBUF =
      MediaType.create("application", "x-protobuf");
  private static final HeldCertificate HELD_CERTIFICATE;
  private static final String canonicalHostName;

  static {
    try {
      canonicalHostName = InetAddress.getByName("localhost").getCanonicalHostName();
      HELD_CERTIFICATE =
          new HeldCertificate.Builder()
              .commonName("localhost")
              .addSubjectAlternativeName(canonicalHostName)
              .build();
    } catch (UnknownHostException e) {
      throw new IllegalStateException("Error building certificate.", e);
    }
  }

  @RegisterExtension
  static MockWebServerExtension server =
      new MockWebServerExtension() {
        @Override
        protected void configureServer(ServerBuilder sb) {
          sb.http(0);
          sb.https(0);
          sb.tls(HELD_CERTIFICATE.keyPair().getPrivate(), HELD_CERTIFICATE.certificate());
        }
      };

  @RegisterExtension LogCapturer logs = LogCapturer.create().captureForType(OkHttpExporter.class);

  private OtlpHttpLogRecordExporterBuilder builder;

  @BeforeEach
  void setup() {
    builder =
        OtlpHttpLogRecordExporter.builder()
            .setEndpoint("http://" + canonicalHostName + ":" + server.httpPort() + "/v1/logs")
            .addHeader("foo", "bar");
  }

  @Test
  @SuppressWarnings("PreferJavaTimeOverload")
  void validConfig() {
    assertThatCode(() -> OtlpHttpLogRecordExporter.builder().setTimeout(0, TimeUnit.MILLISECONDS))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpHttpLogRecordExporter.builder().setTimeout(Duration.ofMillis(0)))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpHttpLogRecordExporter.builder().setTimeout(10, TimeUnit.MILLISECONDS))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpHttpLogRecordExporter.builder().setTimeout(Duration.ofMillis(10)))
        .doesNotThrowAnyException();

    assertThatCode(
            () ->
                OtlpHttpLogRecordExporter.builder()
                    .setEndpoint("http://" + canonicalHostName + ":4317"))
        .doesNotThrowAnyException();
    assertThatCode(
            () ->
                OtlpHttpLogRecordExporter.builder().setEndpoint("http://" + canonicalHostName + ""))
        .doesNotThrowAnyException();
    assertThatCode(
            () ->
                OtlpHttpLogRecordExporter.builder()
                    .setEndpoint("https://" + canonicalHostName + ""))
        .doesNotThrowAnyException();
    assertThatCode(
            () ->
                OtlpHttpLogRecordExporter.builder()
                    .setEndpoint("http://foo:bar@" + canonicalHostName + ""))
        .doesNotThrowAnyException();

    assertThatCode(() -> OtlpHttpLogRecordExporter.builder().setCompression("gzip"))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpHttpLogRecordExporter.builder().setCompression("none"))
        .doesNotThrowAnyException();

    assertThatCode(
            () ->
                OtlpHttpLogRecordExporter.builder().addHeader("foo", "bar").addHeader("baz", "qux"))
        .doesNotThrowAnyException();

    assertThatCode(
            () ->
                OtlpHttpLogRecordExporter.builder()
                    .setTrustedCertificates("foobar".getBytes(StandardCharsets.UTF_8)))
        .doesNotThrowAnyException();

    assertThatCode(
            () ->
                OtlpHttpLogRecordExporter.builder()
                    .setClientTls(
                        "foobar".getBytes(StandardCharsets.UTF_8),
                        "foobar".getBytes(StandardCharsets.UTF_8)))
        .doesNotThrowAnyException();
  }

  @Test
  @SuppressWarnings("PreferJavaTimeOverload")
  void invalidConfig() {
    assertThatThrownBy(
            () -> OtlpHttpLogRecordExporter.builder().setTimeout(-1, TimeUnit.MILLISECONDS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("timeout must be non-negative");
    assertThatThrownBy(() -> OtlpHttpLogRecordExporter.builder().setTimeout(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
    assertThatThrownBy(() -> OtlpHttpLogRecordExporter.builder().setTimeout(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("timeout");

    assertThatThrownBy(() -> OtlpHttpLogRecordExporter.builder().setEndpoint(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("endpoint");
    assertThatThrownBy(
            () -> OtlpHttpLogRecordExporter.builder().setEndpoint("😺://" + canonicalHostName + ""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must be a URL: 😺://" + canonicalHostName + "");
    assertThatThrownBy(
            () -> OtlpHttpLogRecordExporter.builder().setEndpoint("" + canonicalHostName + ""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Invalid endpoint, must start with http:// or https://: " + canonicalHostName + "");
    assertThatThrownBy(
            () ->
                OtlpHttpLogRecordExporter.builder()
                    .setEndpoint("gopher://" + canonicalHostName + ""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Invalid endpoint, must start with http:// or https://: gopher://"
                + canonicalHostName
                + "");

    assertThatThrownBy(() -> OtlpHttpLogRecordExporter.builder().setCompression(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("compressionMethod");
    assertThatThrownBy(() -> OtlpHttpLogRecordExporter.builder().setCompression("foo"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Unsupported compression method. Supported compression methods include: gzip, none.");
  }

  @Test
  void testSetRetryPolicyOnDelegate() {
    assertThatCode(
            () ->
                RetryUtil.setRetryPolicyOnDelegate(
                    OtlpHttpLogRecordExporter.builder(), RetryPolicy.getDefault()))
        .doesNotThrowAnyException();
  }

  @Test
  void testExportUncompressed() {
    server.enqueue(successResponse());
    OtlpHttpLogRecordExporter exporter = builder.build();

    ExportLogsServiceRequest payload = exportAndAssertResult(exporter, /* expectedResult= */ true);
    RecordedRequest recorded = server.takeRequest();
    AggregatedHttpRequest request = recorded.request();
    assertRequestCommon(request);
    assertThat(parseRequestBody(request.content().array())).isEqualTo(payload);

    // OkHttp does not support HTTP/2 upgrade on plaintext.
    assertThat(recorded.context().sessionProtocol().isMultiplex()).isFalse();
  }

  @Test
  void testExportTls() {
    server.enqueue(successResponse());
    OtlpHttpLogRecordExporter exporter =
        builder
            .setEndpoint("https://" + canonicalHostName + ":" + server.httpsPort() + "/v1/logs")
            .setTrustedCertificates(
                HELD_CERTIFICATE.certificatePem().getBytes(StandardCharsets.UTF_8))
            .build();

    ExportLogsServiceRequest payload = exportAndAssertResult(exporter, /* expectedResult= */ true);
    RecordedRequest recorded = server.takeRequest();
    AggregatedHttpRequest request = recorded.request();
    assertRequestCommon(request);
    assertThat(parseRequestBody(request.content().array())).isEqualTo(payload);

    // OkHttp does support HTTP/2 upgrade on TLS.
    assertThat(recorded.context().sessionProtocol().isMultiplex()).isTrue();
  }

  @Test
  void testExportGzipCompressed() {
    server.enqueue(successResponse());
    OtlpHttpLogRecordExporter exporter = builder.setCompression("gzip").build();

    ExportLogsServiceRequest payload = exportAndAssertResult(exporter, /* expectedResult= */ true);
    AggregatedHttpRequest request = server.takeRequest().request();
    assertRequestCommon(request);
    assertThat(request.headers().get("Content-Encoding")).isEqualTo("gzip");
    assertThat(parseRequestBody(gzipDecompress(request.content().array()))).isEqualTo(payload);
  }

  private static void assertRequestCommon(AggregatedHttpRequest request) {
    assertThat(request.method()).isEqualTo(HttpMethod.POST);
    assertThat(request.path()).isEqualTo("/v1/logs");
    assertThat(request.headers().get("foo")).isEqualTo("bar");
    assertThat(request.headers().get("Content-Type")).isEqualTo(APPLICATION_PROTOBUF.toString());
  }

  private static ExportLogsServiceRequest parseRequestBody(byte[] bytes) {
    try {
      return ExportLogsServiceRequest.parseFrom(bytes);
    } catch (InvalidProtocolBufferException e) {
      throw new IllegalStateException("Unable to parse Protobuf request body.", e);
    }
  }

  private static byte[] gzipDecompress(byte[] bytes) {
    try {
      Buffer result = new Buffer();
      GzipSource source = new GzipSource(new Buffer().write(bytes));
      while (source.read(result, Integer.MAX_VALUE) != -1) {}
      return result.readByteArray();
    } catch (IOException e) {
      throw new IllegalStateException("Unable to decompress payload.", e);
    }
  }

  @Test
  @SuppressLogger(OkHttpExporter.class)
  void testServerError() {
    server.enqueue(
        buildResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            Status.newBuilder().setMessage("Server error!").build()));
    OtlpHttpLogRecordExporter exporter = builder.build();

    exportAndAssertResult(exporter, /* expectedResult= */ false);
    LoggingEvent log =
        logs.assertContains(
            "Failed to export logs. Server responded with HTTP status code 500. Error message: Server error!");
    assertThat(log.getLevel()).isEqualTo(Level.WARN);
  }

  @Test
  @SuppressLogger(OkHttpExporter.class)
  void testServerErrorParseError() {
    server.enqueue(
        HttpResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, APPLICATION_PROTOBUF, "Server error!"));
    OtlpHttpLogRecordExporter exporter = builder.build();

    exportAndAssertResult(exporter, /* expectedResult= */ false);
    LoggingEvent log =
        logs.assertContains(
            "Failed to export logs. Server responded with HTTP status code 500. Error message: Unable to parse response body, HTTP status message:");
    assertThat(log.getLevel()).isEqualTo(Level.WARN);
  }

  private static ExportLogsServiceRequest exportAndAssertResult(
      OtlpHttpLogRecordExporter otlpHttpLogRecordExporter, boolean expectedResult) {
    List<LogRecordData> logs = Collections.singletonList(generateFakeLog());
    CompletableResultCode resultCode = otlpHttpLogRecordExporter.export(logs);
    resultCode.join(10, TimeUnit.SECONDS);
    assertThat(resultCode.isSuccess()).isEqualTo(expectedResult);
    List<ResourceLogs> resourceLogs =
        Arrays.stream(ResourceLogsMarshaler.create(logs))
            .map(
                marshaler -> {
                  ByteArrayOutputStream bos = new ByteArrayOutputStream();
                  try {
                    marshaler.writeBinaryTo(bos);
                    return ResourceLogs.parseFrom(bos.toByteArray());
                  } catch (IOException e) {
                    throw new UncheckedIOException(e);
                  }
                })
            .collect(Collectors.toList());
    return ExportLogsServiceRequest.newBuilder().addAllResourceLogs(resourceLogs).build();
  }

  private static HttpResponse successResponse() {
    ExportLogsServiceResponse exportLogsServiceResponse =
        ExportLogsServiceResponse.newBuilder().build();
    return buildResponse(HttpStatus.OK, exportLogsServiceResponse);
  }

  private static <T extends Message> HttpResponse buildResponse(HttpStatus httpStatus, T message) {
    return HttpResponse.of(httpStatus, APPLICATION_PROTOBUF, message.toByteArray());
  }

  private static LogRecordData generateFakeLog() {
    return TestLogRecordData.builder()
        .setResource(Resource.getDefault())
        .setInstrumentationScopeInfo(
            InstrumentationScopeInfo.builder("testLib")
                .setVersion("1.0")
                .setSchemaUrl("http://url")
                .build())
        .setBody("log body")
        .setAttributes(Attributes.builder().put("key", "value").build())
        .setSeverity(Severity.INFO)
        .setSeverityText(Severity.INFO.name())
        .setEpoch(Instant.now())
        .build();
  }
}
