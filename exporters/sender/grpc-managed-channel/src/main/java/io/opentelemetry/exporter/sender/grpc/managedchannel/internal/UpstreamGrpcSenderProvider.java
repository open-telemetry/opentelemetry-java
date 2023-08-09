/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.grpc.managedchannel.internal;

import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.Codec;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.opentelemetry.exporter.internal.grpc.GrpcSender;
import io.opentelemetry.exporter.internal.grpc.GrpcSenderProvider;
import io.opentelemetry.exporter.internal.grpc.MarshalerServiceStub;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.net.URI;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

/**
 * {@link GrpcSender} SPI implementation for {@link UpstreamGrpcSender}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class UpstreamGrpcSenderProvider implements GrpcSenderProvider {

  @Override
  public <T extends Marshaler> GrpcSender<T> createSender(
      URI endpoint,
      String endpointPath,
      boolean compressionEnabled,
      long timeoutNanos,
      Map<String, String> headers,
      @Nullable Object managedChannel,
      Supplier<BiFunction<Channel, String, MarshalerServiceStub<T, ?, ?>>> stubFactory,
      @Nullable RetryPolicy retryPolicy,
      @Nullable SSLContext sslContext,
      @Nullable X509TrustManager trustManager) {
    Metadata metadata = new Metadata();
    String authorityOverride = null;
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      String name = entry.getKey();
      String value = entry.getValue();
      if (name.equals("host")) {
        authorityOverride = value;
        continue;
      }
      metadata.put(Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER), value);
    }

    Channel channel =
        ClientInterceptors.intercept(
            (Channel) managedChannel, MetadataUtils.newAttachHeadersInterceptor(metadata));

    Codec codec = compressionEnabled ? new Codec.Gzip() : Codec.Identity.NONE;
    MarshalerServiceStub<T, ?, ?> stub =
        stubFactory
            .get()
            .apply(channel, authorityOverride)
            .withCompression(codec.getMessageEncoding());

    return new UpstreamGrpcSender<>(stub, timeoutNanos);
  }
}
