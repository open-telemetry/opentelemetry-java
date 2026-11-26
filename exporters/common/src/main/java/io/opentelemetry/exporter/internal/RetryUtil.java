/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import io.opentelemetry.sdk.common.export.GrpcStatusCode;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class RetryUtil {

  private static final Set<String> RETRYABLE_GRPC_STATUS_CODES;
  private static final Set<GrpcStatusCode> RETRYABLE_GRPC_STATUS_CODE_ENUMS =
      Collections.unmodifiableSet(
          EnumSet.of(
              GrpcStatusCode.CANCELLED,
              GrpcStatusCode.DEADLINE_EXCEEDED,
              GrpcStatusCode.RESOURCE_EXHAUSTED,
              GrpcStatusCode.ABORTED,
              GrpcStatusCode.OUT_OF_RANGE,
              GrpcStatusCode.UNAVAILABLE,
              GrpcStatusCode.DATA_LOSS));
  private static final Set<Integer> RETRYABLE_HTTP_STATUS_CODES =
      Collections.unmodifiableSet(new HashSet<>(Arrays.asList(429, 502, 503, 504)));

  static {
    Set<Integer> retryableGrpcStatusCodes = new HashSet<>();
    retryableGrpcStatusCodes.add(GrpcStatusCode.CANCELLED.getValue());
    retryableGrpcStatusCodes.add(GrpcStatusCode.DEADLINE_EXCEEDED.getValue());
    retryableGrpcStatusCodes.add(GrpcStatusCode.RESOURCE_EXHAUSTED.getValue());
    retryableGrpcStatusCodes.add(GrpcStatusCode.ABORTED.getValue());
    retryableGrpcStatusCodes.add(GrpcStatusCode.OUT_OF_RANGE.getValue());
    retryableGrpcStatusCodes.add(GrpcStatusCode.UNAVAILABLE.getValue());
    retryableGrpcStatusCodes.add(GrpcStatusCode.DATA_LOSS.getValue());
    RETRYABLE_GRPC_STATUS_CODES =
        Collections.unmodifiableSet(
            retryableGrpcStatusCodes.stream().map(Object::toString).collect(Collectors.toSet()));
  }

  private RetryUtil() {}

  /** Returns the retryable gRPC status codes. */
  public static Set<String> retryableGrpcStatusCodes() {
    return RETRYABLE_GRPC_STATUS_CODES;
  }

  /** Returns the retryable {@link GrpcStatusCode}s. */
  public static Set<GrpcStatusCode> retryableGrpcStatusCodeEnums() {
    return RETRYABLE_GRPC_STATUS_CODE_ENUMS;
  }

  /** Returns the retryable HTTP status codes. */
  public static Set<Integer> retryableHttpResponseCodes() {
    return RETRYABLE_HTTP_STATUS_CODES;
  }
}
