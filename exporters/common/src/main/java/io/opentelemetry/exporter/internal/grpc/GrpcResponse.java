/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
@AutoValue
public abstract class GrpcResponse {

  GrpcResponse() {}

  public static GrpcResponse create(int grpcStatusValue, @Nullable String grpcStatusDescription) {
    return new AutoValue_GrpcResponse(grpcStatusValue, grpcStatusDescription);
  }

  public abstract int grpcStatusValue();

  @Nullable
  public abstract String grpcStatusDescription();

  // TODO(jack-berg): add byte[] responseBody() throws IOException;
}
