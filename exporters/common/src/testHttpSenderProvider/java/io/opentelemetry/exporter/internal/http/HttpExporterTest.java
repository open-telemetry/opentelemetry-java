/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.exporter.sender.jdk.internal.JdkHttpSender;
import io.opentelemetry.exporter.sender.okhttp.internal.OkHttpHttpSender;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.internal.StandardComponentId;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junitpioneer.jupiter.SetSystemProperty;

class HttpExporterTest {

  @RegisterExtension
  LogCapturer logCapturer =
      LogCapturer.create().captureForLogger(HttpExporterBuilder.class.getName());

  @Test
  @SuppressLogger(HttpExporterBuilder.class)
  void build_multipleSendersNoConfiguration() {
    Assertions.assertThatCode(
            () ->
                new HttpExporterBuilder(
                        StandardComponentId.ExporterType.OTLP_HTTP_SPAN_EXPORTER,
                        "http://localhost")
                    .build())
        .doesNotThrowAnyException();

    logCapturer.assertContains(
        "Multiple HttpSenderProvider found. Please include only one, "
            + "or specify preference setting io.opentelemetry.exporter.http.HttpSenderProvider "
            + "to the FQCN of the preferred provider.");
  }

  @Test
  @SetSystemProperty(
      key = "io.opentelemetry.exporter.internal.http.HttpSenderProvider",
      value = "io.opentelemetry.exporter.sender.jdk.internal.JdkHttpSenderProvider")
  void build_configureUsingOldSpi() {
    assertThat(
            new HttpExporterBuilder(
                    StandardComponentId.ExporterType.OTLP_HTTP_SPAN_EXPORTER, "http://localhost")
                .build())
        .extracting("httpSender")
        .isInstanceOf(JdkHttpSender.class);

    assertThat(logCapturer.getEvents()).isEmpty();
  }

  @Test
  @SetSystemProperty(
      key = "io.opentelemetry.exporter.http.HttpSenderProvider",
      value = "io.opentelemetry.exporter.sender.jdk.internal.JdkHttpSenderProvider")
  void build_multipleSendersWithJdk() {
    assertThat(
            new HttpExporterBuilder(
                    StandardComponentId.ExporterType.OTLP_HTTP_SPAN_EXPORTER, "http://localhost")
                .build())
        .extracting("httpSender")
        .isInstanceOf(JdkHttpSender.class);

    assertThat(logCapturer.getEvents()).isEmpty();
  }

  @Test
  @SetSystemProperty(
      key = "io.opentelemetry.exporter.http.HttpSenderProvider",
      value = "io.opentelemetry.exporter.sender.okhttp.internal.OkHttpHttpSenderProvider")
  void build_multipleSendersWithOkHttp() {
    assertThat(
            new HttpExporterBuilder(
                    StandardComponentId.ExporterType.OTLP_HTTP_SPAN_EXPORTER, "http://localhost")
                .build())
        .extracting("httpSender")
        .isInstanceOf(OkHttpHttpSender.class);

    assertThat(logCapturer.getEvents()).isEmpty();
  }

  @Test
  @SetSystemProperty(key = "io.opentelemetry.exporter.http.HttpSenderProvider", value = "foo")
  void build_multipleSendersNoMatch() {
    assertThatThrownBy(
            () ->
                new HttpExporterBuilder(
                        StandardComponentId.ExporterType.OTLP_HTTP_SPAN_EXPORTER,
                        "http://localhost")
                    .build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "No HttpSenderProvider matched configured io.opentelemetry.exporter.http.HttpSenderProvider: foo");

    assertThat(logCapturer.getEvents()).isEmpty();
  }
}
