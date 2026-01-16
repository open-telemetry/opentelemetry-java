/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.grpc;

import io.opentelemetry.exporter.marshal.MessageWriter;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A gRPC response.
 *
 * @see GrpcSender#send(MessageWriter, Consumer, Consumer)
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
