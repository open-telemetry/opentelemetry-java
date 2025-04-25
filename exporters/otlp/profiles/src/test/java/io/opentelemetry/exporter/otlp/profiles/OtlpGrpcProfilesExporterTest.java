/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractGrpcTelemetryExporterTest;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporter;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporterBuilder;
import io.opentelemetry.exporter.sender.okhttp.internal.OkHttpGrpcSender;
import io.opentelemetry.proto.profiles.v1development.ResourceProfiles;
import java.io.Closeable;
import java.util.List;
import org.junit.jupiter.api.Test;

public class OtlpGrpcProfilesExporterTest
    extends AbstractGrpcTelemetryExporterTest<ProfileData, ResourceProfiles> {

  OtlpGrpcProfilesExporterTest() {
    super("profile", ResourceProfiles.getDefaultInstance());
  }

  @Test
  void usingOkHttp() throws Exception {
    try (Closeable exporter = OtlpGrpcProfilesExporter.builder().build()) {
      assertThat(exporter).extracting("delegate.grpcSender").isInstanceOf(OkHttpGrpcSender.class);
    }
  }

  @Override
  protected TelemetryExporterBuilder<ProfileData> exporterBuilder() {
    return TelemetryExporterBuilder.wrap(OtlpGrpcProfilesExporter.builder());
  }

  @Override
  protected TelemetryExporterBuilder<ProfileData> toBuilder(
      TelemetryExporter<ProfileData> exporter) {
    return TelemetryExporterBuilder.wrap(
        ((OtlpGrpcProfilesExporter) exporter.unwrap()).toBuilder());
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
