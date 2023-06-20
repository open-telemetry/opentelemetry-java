/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.exporter.http.jdk.internal.JdkHttpSender;
import io.opentelemetry.exporter.http.okhttp.internal.OkHttpHttpSender;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junitpioneer.jupiter.SetSystemProperty;

class HttpExporterTest {

  @RegisterExtension
  LogCapturer logCapturer =
      LogCapturer.create().captureForLogger(HttpExporterBuilder.class.getName());

  @Test
  void build_multipleSendersNoConfiguration() {
    Assertions.assertThatCode(
            () -> new HttpExporterBuilder<>("exporter", "type", "http://localhost").build())
        .doesNotThrowAnyException();

    logCapturer.assertContains(
        "Multiple HttpSenderProvider found. Please include only one, "
            + "or specify preference setting io.opentelemetry.exporter.internal.http.HttpSenderProvider "
            + "to the FQCN of the preferred provider.");
  }

  @Test
  @SetSystemProperty(
      key = "io.opentelemetry.exporter.internal.http.HttpSenderProvider",
      value = "io.opentelemetry.exporter.http.jdk.internal.JdkHttpSenderProvider")
  void build_multipleSendersWithJdk() {
    assertThat(new HttpExporterBuilder<>("exporter", "type", "http://localhost").build())
        .extracting("httpSender")
        .isInstanceOf(JdkHttpSender.class);

    assertThat(logCapturer.getEvents()).isEmpty();
  }

  @Test
  @SetSystemProperty(
      key = "io.opentelemetry.exporter.internal.http.HttpSenderProvider",
      value = "io.opentelemetry.exporter.http.okhttp.internal.OkHttpHttpSenderProvider")
  void build_multipleSendersWithOkHttp() {
    assertThat(new HttpExporterBuilder<>("exporter", "type", "http://localhost").build())
        .extracting("httpSender")
        .isInstanceOf(OkHttpHttpSender.class);

    assertThat(logCapturer.getEvents()).isEmpty();
  }

  @Test
  @SetSystemProperty(
      key = "io.opentelemetry.exporter.internal.http.HttpSenderProvider",
      value = "foo")
  void build_multipleSendersNoMatch() {
    assertThatThrownBy(
            () -> new HttpExporterBuilder<>("exporter", "type", "http://localhost").build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "No HttpSenderProvider matched configured io.opentelemetry.exporter.internal.http.HttpSenderProvider: foo");

    assertThat(logCapturer.getEvents()).isEmpty();
  }
}
