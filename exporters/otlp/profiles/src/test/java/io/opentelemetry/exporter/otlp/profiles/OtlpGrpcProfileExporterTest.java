/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.exporter.internal.grpc.GrpcExporter;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractGrpcTelemetryExporterTest;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporter;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporterBuilder;
import io.opentelemetry.exporter.sender.okhttp.internal.OkHttpGrpcSender;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.proto.profiles.v1development.ResourceProfiles;
import io.opentelemetry.sdk.common.export.GrpcStatusCode;
import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

class OtlpGrpcProfileExporterTest
    extends AbstractGrpcTelemetryExporterTest<ProfileData, ResourceProfiles> {

  OtlpGrpcProfileExporterTest() {
    super("profile", ResourceProfiles.getDefaultInstance());
  }

  @Test
  void usingOkHttp() throws Exception {
    try (Closeable exporter = OtlpGrpcProfileExporter.builder().build()) {
      assertThat(exporter).extracting("delegate.grpcSender").isInstanceOf(OkHttpGrpcSender.class);
    }
  }

  @Test
  @Override // whilst profile signal type is in development it uses a different error message
  @SuppressLogger(GrpcExporter.class)
  protected void testExport_Unimplemented() {
    addGrpcError(GrpcStatusCode.UNIMPLEMENTED, "UNIMPLEMENTED");

    TelemetryExporter<ProfileData> exporter = nonRetryingExporter();

    try {
      assertThat(
              exporter
                  .export(Collections.singletonList(generateFakeTelemetry()))
                  .join(10, TimeUnit.SECONDS)
                  .isSuccess())
          .isFalse();

      LoggingEvent log = logs.assertContains("The profile signal type is still under development");
      assertThat(log.getLevel()).isEqualTo(Level.ERROR);
    } finally {
      exporter.shutdown();
    }
  }

  @Override
  protected TelemetryExporterBuilder<ProfileData> exporterBuilder() {
    return TelemetryExporterBuilder.wrap(OtlpGrpcProfileExporter.builder());
  }

  @Override
  protected TelemetryExporterBuilder<ProfileData> toBuilder(
      TelemetryExporter<ProfileData> exporter) {
    return TelemetryExporterBuilder.wrap(((OtlpGrpcProfileExporter) exporter.unwrap()).toBuilder());
  }

  @Override
  protected ProfileData generateFakeTelemetry() {
    return FakeTelemetryUtil.generateFakeProfileData();
  }

  @Override
  protected Marshaler[] toMarshalers(List<ProfileData> telemetry) {
    return ResourceProfilesMarshaler.create(telemetry);
  }
}
