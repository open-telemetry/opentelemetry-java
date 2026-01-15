/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.netmikey.logunit.api.LogCapturer;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.exporter.sender.grpc.managedchannel.internal.UpstreamGrpcSender;
import io.opentelemetry.exporter.sender.okhttp.internal.OkHttpGrpcSender;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.internal.StandardComponentId;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junitpioneer.jupiter.SetSystemProperty;

class GrpcExporterTest {

  @RegisterExtension
  LogCapturer logCapturer =
      LogCapturer.create().captureForLogger(GrpcExporterBuilder.class.getName());

  @Test
  @SuppressLogger(GrpcExporterBuilder.class)
  void build_multipleSendersNoConfiguration() {
    assertThatCode(
            () ->
                new GrpcExporterBuilder(
                        StandardComponentId.ExporterType.OTLP_GRPC_SPAN_EXPORTER,
                        Duration.ofSeconds(10),
                        new URI("http://localhost"),
                        "io.opentelemetry.Dummy/Method")
                    .setChannel(ManagedChannelBuilder.forTarget("localhost").build())
                    .build())
        .doesNotThrowAnyException();

    logCapturer.assertContains(
        "Multiple GrpcSenderProvider found. Please include only one, "
            + "or specify preference setting io.opentelemetry.exporter.grpc.GrpcSenderProvider "
            + "to the FQCN of the preferred provider.");
  }

  @Test
  @SetSystemProperty(
      key = "io.opentelemetry.exporter.grpc.GrpcSenderProvider",
      value =
          "io.opentelemetry.exporter.sender.grpc.managedchannel.internal.UpstreamGrpcSenderProvider")
  void build_multipleSendersWithUpstream() throws URISyntaxException {
    assertThat(
            new GrpcExporterBuilder(
                    StandardComponentId.ExporterType.OTLP_GRPC_SPAN_EXPORTER,
                    Duration.ofSeconds(10),
                    new URI("http://localhost"),
                    "io.opentelemetry.Dummy/Method")
                .setChannel(ManagedChannelBuilder.forTarget("localhost").build())
                .build())
        .extracting("grpcSender")
        .isInstanceOf(UpstreamGrpcSender.class);

    assertThat(logCapturer.getEvents()).isEmpty();
  }

  @Test
  @SetSystemProperty(
      key = "io.opentelemetry.exporter.grpc.GrpcSenderProvider",
      value = "io.opentelemetry.exporter.sender.okhttp.internal.OkHttpGrpcSenderProvider")
  void build_multipleSendersWithOkHttp() throws URISyntaxException {
    assertThat(
            new GrpcExporterBuilder(
                    StandardComponentId.ExporterType.OTLP_GRPC_SPAN_EXPORTER,
                    Duration.ofSeconds(10),
                    new URI("http://localhost"),
                    "io.opentelemetry.Dummy/Method")
                .setChannel(ManagedChannelBuilder.forTarget("localhost").build())
                .build())
        .extracting("grpcSender")
        .isInstanceOf(OkHttpGrpcSender.class);

    assertThat(logCapturer.getEvents()).isEmpty();
  }

  @Test
  @SetSystemProperty(key = "io.opentelemetry.exporter.grpc.GrpcSenderProvider", value = "foo")
  void build_multipleSendersNoMatch() {
    assertThatThrownBy(
            () ->
                new GrpcExporterBuilder(
                        StandardComponentId.ExporterType.OTLP_GRPC_SPAN_EXPORTER,
                        Duration.ofSeconds(10),
                        new URI("http://localhost"),
                        "io.opentelemetry.Dummy/Method")
                    .setChannel(ManagedChannelBuilder.forTarget("localhost").build())
                    .build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "No GrpcSenderProvider matched configured io.opentelemetry.exporter.grpc.GrpcSenderProvider: foo");

    assertThat(logCapturer.getEvents()).isEmpty();
  }
}
