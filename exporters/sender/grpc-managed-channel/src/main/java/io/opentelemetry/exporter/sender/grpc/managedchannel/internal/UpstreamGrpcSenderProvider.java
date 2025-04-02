/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.grpc.managedchannel.internal;

import io.grpc.Channel;
import io.grpc.Codec;
import io.grpc.CompressorRegistry;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.exporter.internal.compression.Compressor;
import io.opentelemetry.exporter.internal.grpc.GrpcSender;
import io.opentelemetry.exporter.internal.grpc.GrpcSenderConfig;
import io.opentelemetry.exporter.internal.grpc.GrpcSenderProvider;
import io.opentelemetry.exporter.internal.grpc.MarshalerServiceStub;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * {@link GrpcSender} SPI implementation for {@link UpstreamGrpcSender}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class UpstreamGrpcSenderProvider implements GrpcSenderProvider {

  @Override
  public <T extends Marshaler> GrpcSender<T> createSender(GrpcSenderConfig<T> grpcSenderConfig) {
    boolean shutdownChannel = false;
    Object managedChannel = grpcSenderConfig.getManagedChannel();
    if (managedChannel == null) {
      // Shutdown the channel as part of the exporter shutdown sequence if
      shutdownChannel = true;
      managedChannel = minimalFallbackManagedChannel(grpcSenderConfig.getEndpoint());
    }

    String authorityOverride = null;
    Map<String, List<String>> headers = grpcSenderConfig.getHeadersSupplier().get();
    if (headers != null) {
      for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
        if (entry.getKey().equals("host") && !entry.getValue().isEmpty()) {
          authorityOverride = entry.getValue().get(0);
        }
      }
    }

    String compression = Codec.Identity.NONE.getMessageEncoding();
    Compressor compressor = grpcSenderConfig.getCompressor();
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
        grpcSenderConfig
            .getStubFactory()
            .get()
            .apply((Channel) managedChannel, authorityOverride)
            .withCompression(compression);

    return new UpstreamGrpcSender<>(
        stub,
        shutdownChannel,
        grpcSenderConfig.getTimeoutNanos(),
        grpcSenderConfig.getHeadersSupplier(),
        grpcSenderConfig.getExecutorService());
  }

  /**
   * If {@link ManagedChannel} is not explicitly set, provide a minimally configured fallback
   * channel to avoid failing initialization.
   *
   * <p>This is required to accommodate autoconfigure with {@code
   * opentelemetry-exporter-sender-grpc-managed-channel} which will always fail to initialize
   * without a fallback channel since there isn't an opportunity to explicitly set the channel.
   *
   * <p>This only incorporates the target address, port, and whether to use plain text. All
   * additional settings are intentionally ignored and must be configured with an explicitly set
   * {@link ManagedChannel}.
   */
  private static ManagedChannel minimalFallbackManagedChannel(URI endpoint) {
    ManagedChannelBuilder<?> channelBuilder =
        ManagedChannelBuilder.forAddress(endpoint.getHost(), endpoint.getPort());
    if (!endpoint.getScheme().equals("https")) {
      channelBuilder.usePlaintext();
    }
    return channelBuilder.build();
  }
}
