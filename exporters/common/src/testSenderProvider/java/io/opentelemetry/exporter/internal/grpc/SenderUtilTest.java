/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.exporter.internal.SenderUtil;
import io.opentelemetry.exporter.sender.grpc.managedchannel.internal.UpstreamGrpcSenderProvider;
import io.opentelemetry.exporter.sender.jdk.internal.JdkHttpSenderProvider;
import io.opentelemetry.exporter.sender.okhttp.internal.OkHttpGrpcSenderProvider;
import io.opentelemetry.exporter.sender.okhttp.internal.OkHttpHttpSenderProvider;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junitpioneer.jupiter.SetSystemProperty;

class SenderUtilTest {

  @RegisterExtension
  LogCapturer logCapturer = LogCapturer.create().captureForLogger(SenderUtil.class.getName());

  private final ComponentLoader componentLoader =
      ComponentLoader.forClassLoader(SenderUtilTest.class.getClassLoader());

  @Test
  @SuppressLogger(SenderUtil.class)
  void resolveGrpcSenderProvider_multipleSendersNoConfiguration() {
    assertThatCode(() -> SenderUtil.resolveGrpcSenderProvider(componentLoader))
        .doesNotThrowAnyException();

    logCapturer.assertContains(
        "Multiple GrpcSenderProvider found. Please include only one, "
            + "or specify preference setting io.opentelemetry.sdk.common.export.GrpcSenderProvider "
            + "to the FQCN of the preferred provider.");
  }

  // TODO: delete test after support for old spi is removed
  @Test
  @SetSystemProperty(
      key = "io.opentelemetry.exporter.internal.grpc.GrpcSenderProvider",
      value =
          "io.opentelemetry.exporter.sender.grpc.managedchannel.internal.UpstreamGrpcSenderProvider")
  void resolveGrpcSenderProvider_configureUsingOldSpi() {
    assertThat(SenderUtil.resolveGrpcSenderProvider(componentLoader))
        .isInstanceOf(UpstreamGrpcSenderProvider.class);

    assertThat(logCapturer.getEvents()).isEmpty();
  }

  @Test
  @SetSystemProperty(
      key = "io.opentelemetry.sdk.common.export.GrpcSenderProvider",
      value =
          "io.opentelemetry.exporter.sender.grpc.managedchannel.internal.UpstreamGrpcSenderProvider")
  void resolveGrpcSenderProvider_multipleSendersWithUpstream() {
    assertThat(SenderUtil.resolveGrpcSenderProvider(componentLoader))
        .isInstanceOf(UpstreamGrpcSenderProvider.class);

    assertThat(logCapturer.getEvents()).isEmpty();
  }

  @Test
  @SetSystemProperty(
      key = "io.opentelemetry.sdk.common.export.GrpcSenderProvider",
      value = "io.opentelemetry.exporter.sender.okhttp.internal.OkHttpGrpcSenderProvider")
  void resolveGrpcSenderProvider_multipleSendersWithOkHttp() {
    assertThat(SenderUtil.resolveGrpcSenderProvider(componentLoader))
        .isInstanceOf(OkHttpGrpcSenderProvider.class);

    assertThat(logCapturer.getEvents()).isEmpty();
  }

  @Test
  @SetSystemProperty(key = "io.opentelemetry.sdk.common.export.GrpcSenderProvider", value = "foo")
  void resolveGrpcSenderProvider_multipleSendersNoMatch() {
    assertThatThrownBy(() -> SenderUtil.resolveGrpcSenderProvider(componentLoader))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "No GrpcSenderProvider matched configured io.opentelemetry.sdk.common.export.GrpcSenderProvider: foo");

    assertThat(logCapturer.getEvents()).isEmpty();
  }

  @Test
  @SuppressLogger(SenderUtil.class)
  void build_multipleSendersNoConfiguration() {
    Assertions.assertThatCode(() -> SenderUtil.resolveHttpSenderProvider(componentLoader))
        .doesNotThrowAnyException();

    logCapturer.assertContains(
        "Multiple HttpSenderProvider found. Please include only one, "
            + "or specify preference setting io.opentelemetry.sdk.common.export.HttpSenderProvider "
            + "to the FQCN of the preferred provider.");
  }

  // TODO: delete test after support for old spi is removed
  @Test
  @SetSystemProperty(
      key = "io.opentelemetry.exporter.internal.http.HttpSenderProvider",
      value = "io.opentelemetry.exporter.sender.jdk.internal.JdkHttpSenderProvider")
  void build_configureUsingOldSpi() {
    assertThat(SenderUtil.resolveHttpSenderProvider(componentLoader))
        .isInstanceOf(JdkHttpSenderProvider.class);

    assertThat(logCapturer.getEvents()).isEmpty();
  }

  @Test
  @SetSystemProperty(
      key = "io.opentelemetry.sdk.common.export.HttpSenderProvider",
      value = "io.opentelemetry.exporter.sender.jdk.internal.JdkHttpSenderProvider")
  void build_multipleSendersWithJdk() {
    assertThat(SenderUtil.resolveHttpSenderProvider(componentLoader))
        .isInstanceOf(JdkHttpSenderProvider.class);

    assertThat(logCapturer.getEvents()).isEmpty();
  }

  @Test
  @SetSystemProperty(
      key = "io.opentelemetry.sdk.common.export.HttpSenderProvider",
      value = "io.opentelemetry.exporter.sender.okhttp.internal.OkHttpHttpSenderProvider")
  void build_multipleSendersWithOkHttp() {
    assertThat(SenderUtil.resolveHttpSenderProvider(componentLoader))
        .isInstanceOf(OkHttpHttpSenderProvider.class);

    assertThat(logCapturer.getEvents()).isEmpty();
  }

  @Test
  @SetSystemProperty(key = "io.opentelemetry.sdk.common.export.HttpSenderProvider", value = "foo")
  void build_multipleSendersNoMatch() {
    assertThatThrownBy(() -> SenderUtil.resolveHttpSenderProvider(componentLoader))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "No HttpSenderProvider matched configured io.opentelemetry.sdk.common.export.HttpSenderProvider: foo");

    assertThat(logCapturer.getEvents()).isEmpty();
  }
}
