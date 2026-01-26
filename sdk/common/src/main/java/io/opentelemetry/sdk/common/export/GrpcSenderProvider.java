/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.export;

/**
 * A service provider interface (SPI) for providing {@link GrpcSender}s backed by different client
 * libraries.
 */
public interface GrpcSenderProvider {

  /** Returns a {@link GrpcSender} configured with the provided config. */
  GrpcSender createSender(GrpcSenderConfig grpcSenderConfig);
}
