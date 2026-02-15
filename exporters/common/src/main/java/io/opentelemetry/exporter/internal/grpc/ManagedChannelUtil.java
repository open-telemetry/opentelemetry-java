/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import static java.util.stream.Collectors.toList;

import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.exporter.internal.RetryUtil;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities for working with gRPC channels.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ManagedChannelUtil {

  /**
   * Convert the {@link RetryPolicy} into a gRPC service config for the {@code serviceName}. The
   * resulting map can be passed to {@link ManagedChannelBuilder#defaultServiceConfig(Map)}.
   */
  public static Map<String, ?> toServiceConfig(String serviceName, RetryPolicy retryPolicy) {
    List<Double> retryableStatusCodes =
        RetryUtil.retryableGrpcStatusCodes().stream().map(Double::parseDouble).collect(toList());

    Map<String, Object> retryConfig = new HashMap<>();
    retryConfig.put("retryableStatusCodes", retryableStatusCodes);
    retryConfig.put("maxAttempts", (double) retryPolicy.getMaxAttempts());
    retryConfig.put("initialBackoff", retryPolicy.getInitialBackoff().toMillis() / 1000.0 + "s");
    retryConfig.put("maxBackoff", retryPolicy.getMaxBackoff().toMillis() / 1000.0 + "s");
    retryConfig.put("backoffMultiplier", retryPolicy.getBackoffMultiplier());

    Map<String, Object> methodConfig = new HashMap<>();
    methodConfig.put(
        "name", Collections.singletonList(Collections.singletonMap("service", serviceName)));
    methodConfig.put("retryPolicy", retryConfig);

    return Collections.singletonMap("methodConfig", Collections.singletonList(methodConfig));
  }

  private ManagedChannelUtil() {}
}
