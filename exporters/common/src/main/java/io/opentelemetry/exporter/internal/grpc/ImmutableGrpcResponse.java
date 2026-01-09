/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import com.google.auto.value.AutoValue;
import io.opentelemetry.exporter.grpc.GrpcResponse;
import io.opentelemetry.exporter.grpc.GrpcStatusCode;
import javax.annotation.Nullable;

/**
 * Auto value implementation of {@link GrpcResponse}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@AutoValue
public abstract class ImmutableGrpcResponse implements GrpcResponse {

  public static ImmutableGrpcResponse create(
      GrpcStatusCode statusCode, @Nullable String statusDescription, byte[] responseBody) {
    return new AutoValue_ImmutableGrpcResponse(statusCode, statusDescription, responseBody);
  }

  @SuppressWarnings("mutable")
  @Override
  public abstract byte[] getResponseMessage();
}
