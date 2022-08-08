/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.retry;

import io.opentelemetry.exporter.internal.grpc.GrpcExporterBuilder;
import io.opentelemetry.exporter.internal.grpc.GrpcStatusUtil;
import io.opentelemetry.exporter.internal.okhttp.OkHttpExporterBuilder;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public class RetryUtil {

  private static final Set<String> RETRYABLE_GRPC_STATUS_CODES;
  private static final Set<Integer> RETRYABLE_HTTP_STATUS_CODES =
      Collections.unmodifiableSet(new HashSet<>(Arrays.asList(429, 502, 503, 504)));

  static {
    Set<String> retryableGrpcStatusCodes = new HashSet<>();
    retryableGrpcStatusCodes.add(GrpcStatusUtil.GRPC_STATUS_CANCELLED);
    retryableGrpcStatusCodes.add(GrpcStatusUtil.GRPC_STATUS_DEADLINE_EXCEEDED);
    retryableGrpcStatusCodes.add(GrpcStatusUtil.GRPC_STATUS_RESOURCE_EXHAUSTED);
    retryableGrpcStatusCodes.add(GrpcStatusUtil.GRPC_STATUS_ABORTED);
    retryableGrpcStatusCodes.add(GrpcStatusUtil.GRPC_STATUS_OUT_OF_RANGE);
    retryableGrpcStatusCodes.add(GrpcStatusUtil.GRPC_STATUS_UNAVAILABLE);
    retryableGrpcStatusCodes.add(GrpcStatusUtil.GRPC_STATUS_DATA_LOSS);
    RETRYABLE_GRPC_STATUS_CODES = Collections.unmodifiableSet(retryableGrpcStatusCodes);
  }

  private RetryUtil() {}

  /** Returns the retryable gRPC status codes. */
  public static Set<String> retryableGrpcStatusCodes() {
    return RETRYABLE_GRPC_STATUS_CODES;
  }

  /** Returns the retryable HTTP status codes. */
  public static Set<Integer> retryableHttpResponseCodes() {
    return RETRYABLE_HTTP_STATUS_CODES;
  }

  /**
   * Reflectively access a {@link GrpcExporterBuilder}, or {@link OkHttpExporterBuilder} instance in
   * field called "delegate" of the instance, and set the {@link RetryPolicy}.
   *
   * @throws IllegalArgumentException if the instance does not contain a field called "delegate" of
   *     a supported type.
   */
  public static void setRetryPolicyOnDelegate(Object instance, RetryPolicy retryPolicy) {
    try {
      Field field = instance.getClass().getDeclaredField("delegate");
      field.setAccessible(true);
      Object value = field.get(instance);
      if (value instanceof GrpcExporterBuilder) {
        ((GrpcExporterBuilder<?>) value).setRetryPolicy(retryPolicy);
      } else if (value instanceof OkHttpExporterBuilder) {
        ((OkHttpExporterBuilder<?>) value).setRetryPolicy(retryPolicy);
      } else {
        throw new IllegalArgumentException(
            "delegate field is not type DefaultGrpcExporterBuilder or OkHttpGrpcExporterBuilder");
      }
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new IllegalArgumentException("Unable to access delegate reflectively.", e);
    }
  }
}
