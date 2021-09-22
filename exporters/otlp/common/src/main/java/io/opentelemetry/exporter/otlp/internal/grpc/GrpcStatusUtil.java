/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.grpc;

import static io.grpc.Status.Code.ABORTED;
import static io.grpc.Status.Code.CANCELLED;
import static io.grpc.Status.Code.DATA_LOSS;
import static io.grpc.Status.Code.DEADLINE_EXCEEDED;
import static io.grpc.Status.Code.OUT_OF_RANGE;
import static io.grpc.Status.Code.RESOURCE_EXHAUSTED;
import static io.grpc.Status.Code.UNAVAILABLE;

import io.grpc.Status;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class GrpcStatusUtil {

  private static final Set<Status.Code> RETRYABLE_CODES =
      Collections.unmodifiableSet(
          new HashSet<>(
              Arrays.asList(
                  CANCELLED,
                  DEADLINE_EXCEEDED,
                  RESOURCE_EXHAUSTED,
                  ABORTED,
                  OUT_OF_RANGE,
                  UNAVAILABLE,
                  DATA_LOSS)));

  /** Return the set of status codes which are retryable according to the OTLP specification. */
  public static Set<Status.Code> otlpRetryableStatusCodes() {
    return RETRYABLE_CODES;
  }

  /** Determine if the throwable contains a retryable status code. */
  public static boolean hasOtlpRetryableStatusCode(Throwable t) {
    return RETRYABLE_CODES.contains(Status.fromThrowable(t).getCode());
  }

  private GrpcStatusUtil() {}
}
