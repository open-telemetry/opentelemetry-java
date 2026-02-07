/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.export;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A gRPC response.
 *
 * @see GrpcSender#send(MessageWriter, Consumer, Consumer)
 * @since 1.59.0
 */
@Immutable
public interface GrpcResponse {

  /** The response gRPC status code. */
  GrpcStatusCode getStatusCode();

  /** A string description of the status. */
  @Nullable
  String getStatusDescription();

  /** The gRPC response message bytes. */
  @SuppressWarnings("mutable")
  byte[] getResponseMessage();
}
