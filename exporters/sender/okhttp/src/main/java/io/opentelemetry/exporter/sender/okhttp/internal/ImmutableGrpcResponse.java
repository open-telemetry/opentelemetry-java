/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.common.export.GrpcResponse;
import io.opentelemetry.sdk.common.export.GrpcStatusCode;
import javax.annotation.Nullable;

@AutoValue
abstract class ImmutableGrpcResponse implements GrpcResponse {

  static ImmutableGrpcResponse create(
      GrpcStatusCode statusCode, @Nullable String statusDescription, byte[] responseMessage) {
    return new AutoValue_ImmutableGrpcResponse(statusCode, statusDescription, responseMessage);
  }

  @SuppressWarnings("mutable")
  @Override
  public abstract byte[] getResponseMessage();
}
