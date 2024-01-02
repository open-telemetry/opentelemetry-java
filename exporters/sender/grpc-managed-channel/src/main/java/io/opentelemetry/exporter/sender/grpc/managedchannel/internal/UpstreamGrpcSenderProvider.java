/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.grpc.managedchannel.internal;

import io.grpc.Channel;
import io.grpc.Codec;
import io.grpc.CompressorRegistry;
import io.opentelemetry.exporter.internal.compression.Compressor;
import io.opentelemetry.exporter.internal.grpc.GrpcSender;
import io.opentelemetry.exporter.internal.grpc.GrpcSenderProvider;
import io.opentelemetry.exporter.internal.grpc.MarshalerServiceStub;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
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
      @Nullable Compressor compressor,
      long timeoutNanos,
      Supplier<Map<String, List<String>>> headersSupplier,
      @Nullable Object managedChannel,
      Supplier<BiFunction<Channel, String, MarshalerServiceStub<T, ?, ?>>> stubFactory,
      @Nullable RetryPolicy retryPolicy,
      @Nullable SSLContext sslContext,
      @Nullable X509TrustManager trustManager) {
    String authorityOverride = null;
    Map<String, List<String>> headers = headersSupplier.get();
    if (headers != null) {
      for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
        if (entry.getKey().equals("host") && !entry.getValue().isEmpty()) {
          authorityOverride = entry.getValue().get(0);
        }
      }
    }

    String compression = Codec.Identity.NONE.getMessageEncoding();
    if (compressor != null) {
      CompressorRegistry.getDefaultInstance()
          .register(
              new io.grpc.Compressor() {
                @Override
                public String getMessageEncoding() {
                  return compressor.getEncoding();
                }

                @Override
                public OutputStream compress(OutputStream os) throws IOException {
                  return compressor.compress(os);
                }
              });
      compression = compressor.getEncoding();
    }

    MarshalerServiceStub<T, ?, ?> stub =
        stubFactory
            .get()
            .apply((Channel) managedChannel, authorityOverride)
            .withCompression(compression);

    return new UpstreamGrpcSender<>(stub, timeoutNanos, headersSupplier);
  }
}
