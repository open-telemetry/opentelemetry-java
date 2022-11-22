/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.Codec;
import io.grpc.ManagedChannel;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import java.net.URI;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GrpcExporterBuilderTest {

  private final ManagedChannel channel = mock(ManagedChannel.class);

  private GrpcExporterBuilder<Marshaler> builder;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    Supplier<BiFunction<Channel, String, MarshalerServiceStub<Marshaler, ?, ?>>> grpcStubFactory =
        mock(Supplier.class);
    when(grpcStubFactory.get())
        .thenReturn((c, s) -> new TestMarshalerServiceStub(c, CallOptions.DEFAULT));

    builder =
        GrpcExporter.builder(
            "otlp", "span", 0, URI.create("http://localhost:4317"), grpcStubFactory, "/test");
  }

  @Test
  void compressionDefault() {
    GrpcExporter<Marshaler> exporter = builder.build();
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              OkHttpGrpcExporter.class,
              otlp -> assertThat(otlp).extracting("compressionEnabled").isEqualTo(false));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void compressionNone() {
    GrpcExporter<Marshaler> exporter = builder.setCompression("none").build();
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              OkHttpGrpcExporter.class,
              otlp -> assertThat(otlp).extracting("compressionEnabled").isEqualTo(false));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void compressionGzip() {
    GrpcExporter<Marshaler> exporter = builder.setCompression("gzip").build();
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              OkHttpGrpcExporter.class,
              otlp -> assertThat(otlp).extracting("compressionEnabled").isEqualTo(true));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void compressionEnabledAndDisabled() {
    GrpcExporter<Marshaler> exporter =
        builder.setCompression("gzip").setCompression("none").build();
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              OkHttpGrpcExporter.class,
              otlp -> assertThat(otlp).extracting("compressionEnabled").isEqualTo(false));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void compressionDefaultWithChannel() {
    GrpcExporter<Marshaler> exporter = builder.setChannel(channel).build();
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              UpstreamGrpcExporter.class,
              otlp ->
                  assertThat(otlp)
                      .extracting("stub")
                      .extracting("callOptions.compressorName")
                      .isEqualTo(Codec.Identity.NONE.getMessageEncoding()));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void compressionNoneWithChannel() {
    GrpcExporter<Marshaler> exporter = builder.setChannel(channel).setCompression("none").build();
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              UpstreamGrpcExporter.class,
              otlp ->
                  assertThat(otlp)
                      .extracting("stub")
                      .extracting("callOptions.compressorName")
                      .isEqualTo(Codec.Identity.NONE.getMessageEncoding()));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void compressionGzipWithChannel() {
    GrpcExporter<Marshaler> exporter = builder.setChannel(channel).setCompression("gzip").build();
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              UpstreamGrpcExporter.class,
              otlp ->
                  assertThat(otlp)
                      .extracting("stub")
                      .extracting("callOptions.compressorName")
                      .isEqualTo(new Codec.Gzip().getMessageEncoding()));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void compressionEnabledAndDisabledWithChannel() {
    GrpcExporter<Marshaler> exporter =
        builder.setChannel(channel).setCompression("gzip").setCompression("none").build();
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              UpstreamGrpcExporter.class,
              otlp ->
                  assertThat(otlp)
                      .extracting("stub")
                      .extracting("callOptions.compressorName")
                      .isEqualTo(Codec.Identity.NONE.getMessageEncoding()));
    } finally {
      exporter.shutdown();
    }
  }

  private final class TestMarshalerServiceStub
      extends MarshalerServiceStub<Marshaler, Void, TestMarshalerServiceStub> {

    private TestMarshalerServiceStub(Channel channel, CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected TestMarshalerServiceStub build(Channel channel, CallOptions callOptions) {
      return new TestMarshalerServiceStub(channel, callOptions);
    }

    @Override
    public ListenableFuture<Void> export(Marshaler request) {
      return Futures.immediateVoidFuture();
    }
  }
}
