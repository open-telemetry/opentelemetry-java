/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.export;

/**
 * gRPC status codes. See <a href="https://grpc.io/docs/guides/status-codes/">official grpc.io
 * docs</a> for usage.
 */
public enum GrpcStatusCode {
  OK(0),
  CANCELLED(1),
  UNKNOWN(2),
  INVALID_ARGUMENT(3),
  DEADLINE_EXCEEDED(4),
  NOT_FOUND(5),
  ALREADY_EXISTS(6),
  PERMISSION_DENIED(7),
  RESOURCE_EXHAUSTED(8),
  FAILED_PRECONDITION(9),
  ABORTED(10),
  OUT_OF_RANGE(11),
  UNIMPLEMENTED(12),
  INTERNAL(13),
  UNAVAILABLE(14),
  DATA_LOSS(15),
  UNAUTHENTICATED(16);

  private final int value;

  GrpcStatusCode(int value) {
    this.value = value;
  }

  /** Returns the integer representation of the status code. */
  public int getValue() {
    return value;
  }

  /**
   * Returns the {@link GrpcStatusCode} corresponding to the integer {@code value}, or {@link
   * GrpcStatusCode#UNKNOWN} if the {@code value} is not recognized.
   */
  public static GrpcStatusCode fromValue(int value) {
    GrpcStatusCode[] values = GrpcStatusCode.values();
    if (value >= 0 && value < values.length) {
      return values[value];
    }
    return UNKNOWN;
  }
}
