/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import io.opentelemetry.api.internal.ConfigUtil;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.common.export.GrpcSenderProvider;
import io.opentelemetry.sdk.common.export.HttpSenderProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilities for loading senders.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class SenderUtil {

  private static final Logger LOGGER = Logger.getLogger(SenderUtil.class.getName());

  private static final String OLD_GRPC_SPI_PROPERTY =
      "io.opentelemetry.exporter.internal.grpc.GrpcSenderProvider";
  private static final String GRPC_SPI_PROPERTY =
      "io.opentelemetry.sdk.common.export.GrpcSenderProvider";
  private static final String OLD_HTTP_SPI_PROPERTY =
      "io.opentelemetry.exporter.internal.http.HttpSenderProvider";
  private static final String HTTP_SPI_PROPERTY =
      "io.opentelemetry.sdk.common.export.HttpSenderProvider";

  private SenderUtil() {}

  /**
   * Resolve the {@link GrpcSenderProvider}.
   *
   * <p>If no {@link GrpcSenderProvider} is available, throw {@link IllegalStateException}.
   *
   * <p>If only one {@link GrpcSenderProvider} is available, use it.
   *
   * <p>If multiple are available and..
   *
   * <ul>
   *   <li>{@code io.opentelemetry.sdk.common.export.GrpcSenderProvider} is empty, use the first
   *       found.
   *   <li>{@code io.opentelemetry.sdk.common.export.GrpcSenderProvider} is set, use the matching
   *       provider. If none match, throw {@link IllegalStateException}.
   * </ul>
   */
  public static GrpcSenderProvider resolveGrpcSenderProvider(ComponentLoader componentLoader) {
    Map<String, GrpcSenderProvider> grpcSenderProviders = new HashMap<>();
    for (GrpcSenderProvider spi : componentLoader.load(GrpcSenderProvider.class)) {
      grpcSenderProviders.put(spi.getClass().getName(), spi);
    }

    // No provider on classpath, throw
    if (grpcSenderProviders.isEmpty()) {
      throw new IllegalStateException(
          "No GrpcSenderProvider found on classpath. Please add dependency on "
              + "opentelemetry-exporter-sender-okhttp or opentelemetry-exporter-sender-grpc-managed-channel");
    }

    // Exactly one provider on classpath, use it
    if (grpcSenderProviders.size() == 1) {
      return grpcSenderProviders.values().stream().findFirst().get();
    }

    // If we've reached here, there are multiple GrpcSenderProviders
    String configuredSender = ConfigUtil.getString(GRPC_SPI_PROPERTY, "");
    // TODO: remove support for reading OLD_SPI_PROPERTY after 1.61.0
    if (configuredSender.isEmpty()) {
      configuredSender = ConfigUtil.getString(OLD_GRPC_SPI_PROPERTY, "");
      if (!configuredSender.isEmpty()) {
        LOGGER.log(
            Level.WARNING,
            OLD_GRPC_SPI_PROPERTY
                + " was used to set GrpcSenderProvider. Please use "
                + GRPC_SPI_PROPERTY
                + " instead. "
                + OLD_GRPC_SPI_PROPERTY
                + " will be removed after 1.61.0");
      }
    }

    // Multiple providers but none configured, use first we find and log a warning
    if (configuredSender.isEmpty()) {
      LOGGER.log(
          Level.WARNING,
          "Multiple GrpcSenderProvider found. Please include only one, "
              + "or specify preference setting "
              + GRPC_SPI_PROPERTY
              + " to the FQCN of the preferred provider.");
      return grpcSenderProviders.values().stream().findFirst().get();
    }

    // Multiple providers with configuration match, use configuration match
    if (grpcSenderProviders.containsKey(configuredSender)) {
      return grpcSenderProviders.get(configuredSender);
    }

    // Multiple providers, configured does not match, throw
    throw new IllegalStateException(
        "No GrpcSenderProvider matched configured " + GRPC_SPI_PROPERTY + ": " + configuredSender);
  }

  /**
   * Resolve the {@link HttpSenderProvider}.
   *
   * <p>If no {@link HttpSenderProvider} is available, throw {@link IllegalStateException}.
   *
   * <p>If only one {@link HttpSenderProvider} is available, use it.
   *
   * <p>If multiple are available and..
   *
   * <ul>
   *   <li>{@code io.opentelemetry.sdk.common.export.HttpSenderProvider} is empty, use the first
   *       found.
   *   <li>{@code io.opentelemetry.sdk.common.export.HttpSenderProvider} is set, use the matching
   *       provider. If none match, throw {@link IllegalStateException}.
   * </ul>
   */
  public static HttpSenderProvider resolveHttpSenderProvider(ComponentLoader componentLoader) {
    Map<String, HttpSenderProvider> httpSenderProviders = new HashMap<>();
    for (HttpSenderProvider spi : componentLoader.load(HttpSenderProvider.class)) {
      httpSenderProviders.put(spi.getClass().getName(), spi);
    }

    // No provider on classpath, throw
    if (httpSenderProviders.isEmpty()) {
      throw new IllegalStateException(
          "No HttpSenderProvider found on classpath. Please add dependency on "
              + "opentelemetry-exporter-sender-okhttp or opentelemetry-exporter-sender-jdk");
    }

    // Exactly one provider on classpath, use it
    if (httpSenderProviders.size() == 1) {
      return httpSenderProviders.values().stream().findFirst().get();
    }

    // If we've reached here, there are multiple HttpSenderProviders
    String configuredSender = ConfigUtil.getString(HTTP_SPI_PROPERTY, "");
    // TODO: remove support for reading OLD_SPI_PROPERTY after 1.61.0
    if (configuredSender.isEmpty()) {
      configuredSender = ConfigUtil.getString(OLD_HTTP_SPI_PROPERTY, "");
      if (!configuredSender.isEmpty()) {
        LOGGER.log(
            Level.WARNING,
            OLD_HTTP_SPI_PROPERTY
                + " was used to set HttpSenderProvider. Please use "
                + HTTP_SPI_PROPERTY
                + " instead. "
                + OLD_HTTP_SPI_PROPERTY
                + " will be removed after 1.61.0");
      }
    }

    // Multiple providers but none configured, use first we find and log a warning
    if (configuredSender.isEmpty()) {
      LOGGER.log(
          Level.WARNING,
          "Multiple HttpSenderProvider found. Please include only one, "
              + "or specify preference setting "
              + HTTP_SPI_PROPERTY
              + " to the FQCN of the preferred provider.");
      return httpSenderProviders.values().stream().findFirst().get();
    }

    // Multiple providers with configuration match, use configuration match
    if (httpSenderProviders.containsKey(configuredSender)) {
      return httpSenderProviders.get(configuredSender);
    }

    // Multiple providers, configured does not match, throw
    throw new IllegalStateException(
        "No HttpSenderProvider matched configured " + HTTP_SPI_PROPERTY + ": " + configuredSender);
  }
}
