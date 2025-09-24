/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.github.netmikey.logunit.api.LogCapturer;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.sender.grpc.managedchannel.internal.UpstreamGrpcSender;
import io.opentelemetry.exporter.sender.okhttp.internal.OkHttpGrpcSender;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.internal.StandardComponentId;
import java.net.URI;
import java.net.URISyntaxException;
import javax.annotation.Nullable;
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
                new GrpcExporterBuilder<>(
                        StandardComponentId.ExporterType.OTLP_GRPC_SPAN_EXPORTER,
                        10,
                        new URI("http://localhost"),
                        () -> DummyServiceFutureStub::newFutureStub,
                        "/path")
                    .setChannel(ManagedChannelBuilder.forTarget("localhost").build())
                    .build())
        .doesNotThrowAnyException();

    logCapturer.assertContains(
        "Multiple GrpcSenderProvider found. Please include only one, "
            + "or specify preference setting io.opentelemetry.exporter.internal.grpc.GrpcSenderProvider "
            + "to the FQCN of the preferred provider.");
  }

  @Test
  @SetSystemProperty(
      key = "io.opentelemetry.exporter.internal.grpc.GrpcSenderProvider",
      value =
          "io.opentelemetry.exporter.sender.grpc.managedchannel.internal.UpstreamGrpcSenderProvider")
  void build_multipleSendersWithUpstream() throws URISyntaxException {
    assertThat(
            new GrpcExporterBuilder<>(
                    StandardComponentId.ExporterType.OTLP_GRPC_SPAN_EXPORTER,
                    10,
                    new URI("http://localhost"),
                    () -> DummyServiceFutureStub::newFutureStub,
                    "/path")
                .setChannel(ManagedChannelBuilder.forTarget("localhost").build())
                .build())
        .extracting("grpcSender")
        .isInstanceOf(UpstreamGrpcSender.class);

    assertThat(logCapturer.getEvents()).isEmpty();
  }

  @Test
  @SetSystemProperty(
      key = "io.opentelemetry.exporter.internal.grpc.GrpcSenderProvider",
      value = "io.opentelemetry.exporter.sender.okhttp.internal.OkHttpGrpcSenderProvider")
  void build_multipleSendersWithOkHttp() throws URISyntaxException {
    assertThat(
            new GrpcExporterBuilder<>(
                    StandardComponentId.ExporterType.OTLP_GRPC_SPAN_EXPORTER,
                    10,
                    new URI("http://localhost"),
                    () -> DummyServiceFutureStub::newFutureStub,
                    "/path")
                .setChannel(ManagedChannelBuilder.forTarget("localhost").build())
                .build())
        .extracting("grpcSender")
        .isInstanceOf(OkHttpGrpcSender.class);

    assertThat(logCapturer.getEvents()).isEmpty();
  }

  @Test
  @SetSystemProperty(
      key = "io.opentelemetry.exporter.internal.grpc.GrpcSenderProvider",
      value = "foo")
  void build_multipleSendersNoMatch() {
    assertThatThrownBy(
            () ->
                new GrpcExporterBuilder<>(
                        StandardComponentId.ExporterType.OTLP_GRPC_SPAN_EXPORTER,
                        10,
                        new URI("http://localhost"),
                        () -> DummyServiceFutureStub::newFutureStub,
                        "/path")
                    .setChannel(ManagedChannelBuilder.forTarget("localhost").build())
                    .build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "No GrpcSenderProvider matched configured io.opentelemetry.exporter.internal.grpc.GrpcSenderProvider: foo");

    assertThat(logCapturer.getEvents()).isEmpty();
  }

  private static class DummyServiceFutureStub
      extends MarshalerServiceStub<DummyMarshaler, Object, DummyServiceFutureStub> {

    private DummyServiceFutureStub(Channel channel, CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    public ListenableFuture<Object> export(DummyMarshaler request) {
      SettableFuture<Object> future = SettableFuture.create();
      future.set(new Object());
      return future;
    }

    @Override
    protected DummyServiceFutureStub build(Channel channel, CallOptions callOptions) {
      return new DummyServiceFutureStub(channel, callOptions);
    }

    private static DummyServiceFutureStub newFutureStub(
        io.grpc.Channel channel, @Nullable String authorityOverride) {
      return DummyServiceFutureStub.newStub(
          (c, options) -> new DummyServiceFutureStub(c, options.withAuthority(authorityOverride)),
          channel);
    }
  }

  private static class DummyMarshaler extends MarshalerWithSize {

    private DummyMarshaler() {
      super(0);
    }

    @Override
    protected void writeTo(Serializer output) {}
  }
}
