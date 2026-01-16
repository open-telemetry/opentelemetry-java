/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.grpc.managedchannel.internal;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.exporter.grpc.GrpcSender;
import io.opentelemetry.exporter.grpc.GrpcSenderConfig;
import io.opentelemetry.exporter.grpc.GrpcSenderProvider;
import io.opentelemetry.exporter.internal.grpc.ExtendedGrpcSenderConfig;
import java.net.URI;

/**
 * {@link GrpcSender} SPI implementation for {@link UpstreamGrpcSender}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class UpstreamGrpcSenderProvider implements GrpcSenderProvider {

  @Override
  public GrpcSender createSender(GrpcSenderConfig grpcSenderConfig) {
    if (!(grpcSenderConfig instanceof ExtendedGrpcSenderConfig)) {
      throw new IllegalStateException(
          "grpcSenderConfig must be an instance of ExtendedGrpcSenderConfig for UpstreamGrpcSenderProvider");
    }
    ExtendedGrpcSenderConfig extendedSenderConfig = (ExtendedGrpcSenderConfig) grpcSenderConfig;

    boolean shutdownChannel = false;
    Object configManagedChannel = extendedSenderConfig.getMangedChannel();
    ManagedChannel managedChannel;
    if (configManagedChannel != null) {
      if (!(configManagedChannel instanceof ManagedChannel)) {
        throw new IllegalStateException(
            "managedChannel must be an instance of ManagedChannel for UpstreamGrpcSenderProvider");
      }
      managedChannel = (ManagedChannel) configManagedChannel;
    } else {
      // Shutdown the channel as part of the exporter shutdown sequence if
      shutdownChannel = true;
      managedChannel = minimalFallbackManagedChannel(grpcSenderConfig.getEndpoint());
    }

    return new UpstreamGrpcSender(
        managedChannel,
        extendedSenderConfig.getFullMethodName(),
        extendedSenderConfig.getCompressor(),
        shutdownChannel,
        extendedSenderConfig.getTimeout(),
        extendedSenderConfig.getHeadersSupplier(),
        extendedSenderConfig.getExecutorService());
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
