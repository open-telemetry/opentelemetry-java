/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.grpc.OkHttpGrpcExporterBuilder;
import io.opentelemetry.exporter.otlp.internal.logs.ResourceLogsMarshaler;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractGrpcTelemetryExporterTest;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.LogDataBuilder;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.resources.Resource;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class OtlpGrpcLogsExporterTest
    extends AbstractGrpcTelemetryExporterTest<LogData, ResourceLogs, OtlpGrpcLogExporter> {

  OtlpGrpcLogsExporterTest() {
    super("log", ResourceLogs.getDefaultInstance());
  }

  @Override
  protected OtlpGrpcLogExporter createExporter(String endpoint) {
    return OtlpGrpcLogExporter.builder().setEndpoint(endpoint).build();
  }

  @Override
  protected OtlpGrpcLogExporter createExporterWithTimeout(String endpoint, Duration timeout) {
    return OtlpGrpcLogExporter.builder().setEndpoint(endpoint).setTimeout(timeout).build();
  }

  @Override
  protected CompletableResultCode shutdownExporter(OtlpGrpcLogExporter exporter) {
    return exporter.shutdown();
  }

  @Override
  protected CompletableResultCode doExport(OtlpGrpcLogExporter exporter, List<LogData> telemetry) {
    return exporter.export(telemetry);
  }

  @Override
  protected LogData generateFakeTelemetry() {
    return LogDataBuilder.create(
            Resource.create(Attributes.builder().put("testKey", "testValue").build()),
            InstrumentationLibraryInfo.create("instrumentation", "1"))
        .setEpoch(Instant.now())
        .setSeverity(Severity.ERROR)
        .setSeverityText("really severe")
        .setName("log1")
        .setBody("message")
        .setAttributes(Attributes.builder().put("animal", "cat").build())
        .build();
  }

  @Override
  protected Marshaler[] toMarshalers(List<LogData> telemetry) {
    return ResourceLogsMarshaler.create(telemetry);
  }

  @Test
  @SuppressWarnings("PreferJavaTimeOverload")
  void validConfig() {
    assertThatCode(() -> OtlpGrpcLogExporter.builder().setTimeout(0, TimeUnit.MILLISECONDS))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcLogExporter.builder().setTimeout(Duration.ofMillis(0)))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcLogExporter.builder().setTimeout(10, TimeUnit.MILLISECONDS))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcLogExporter.builder().setTimeout(Duration.ofMillis(10)))
        .doesNotThrowAnyException();

    assertThatCode(() -> OtlpGrpcLogExporter.builder().setEndpoint("http://localhost:4317"))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcLogExporter.builder().setEndpoint("http://localhost"))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcLogExporter.builder().setEndpoint("https://localhost"))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcLogExporter.builder().setEndpoint("http://foo:bar@localhost"))
        .doesNotThrowAnyException();

    assertThatCode(() -> OtlpGrpcLogExporter.builder().setCompression("gzip"))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcLogExporter.builder().setCompression("none"))
        .doesNotThrowAnyException();

    assertThatCode(
            () -> OtlpGrpcLogExporter.builder().addHeader("foo", "bar").addHeader("baz", "qux"))
        .doesNotThrowAnyException();

    assertThatCode(
            () ->
                OtlpGrpcLogExporter.builder()
                    .setTrustedCertificates("foobar".getBytes(StandardCharsets.UTF_8)))
        .doesNotThrowAnyException();
  }

  @Test
  @SuppressWarnings("PreferJavaTimeOverload")
  void invalidConfig() {
    assertThatThrownBy(() -> OtlpGrpcLogExporter.builder().setTimeout(-1, TimeUnit.MILLISECONDS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("timeout must be non-negative");
    assertThatThrownBy(() -> OtlpGrpcLogExporter.builder().setTimeout(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
    assertThatThrownBy(() -> OtlpGrpcLogExporter.builder().setTimeout(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("timeout");

    assertThatThrownBy(() -> OtlpGrpcLogExporter.builder().setEndpoint(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("endpoint");
    assertThatThrownBy(() -> OtlpGrpcLogExporter.builder().setEndpoint("😺://localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must be a URL: 😺://localhost");
    assertThatThrownBy(() -> OtlpGrpcLogExporter.builder().setEndpoint("localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must start with http:// or https://: localhost");
    assertThatThrownBy(() -> OtlpGrpcLogExporter.builder().setEndpoint("gopher://localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must start with http:// or https://: gopher://localhost");

    assertThatThrownBy(() -> OtlpGrpcLogExporter.builder().setCompression(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("compressionMethod");
    assertThatThrownBy(() -> OtlpGrpcLogExporter.builder().setCompression("foo"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Unsupported compression method. Supported compression methods include: gzip, none.");
  }

  @Test
  void usingOkHttp() {
    assertThat(OtlpGrpcLogExporter.builder().delegate)
        .isInstanceOf(OkHttpGrpcExporterBuilder.class);
  }
}
