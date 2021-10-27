/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import java.util.Map;

/** Utilities to help deal w/ {@code Map<Attributes, Accumulation>} in metric storage. */
final class MetricStorageUtils {
  private MetricStorageUtils() {}

  /**
   * Merges accumulations from {@code toMerge} into {@code result}.
   *
   * <p>Note: This mutates the result map.
   */
  static <T> void mergeInPlace(
      Map<Attributes, T> result, Map<Attributes, T> toMerge, Aggregator<T> aggregator) {
    toMerge.forEach(
        (k, v) -> {
          result.compute(k, (k2, v2) -> (v2 != null) ? aggregator.merge(v2, v) : v);
        });
  }

  /**
   * Diffs accumulations from {@code toMerge} into {@code result}.
   *
   * <p>If no prior value is found, then the value from {@code toDiff} is used.
   *
   * <p>Note: This mutates the result map.
   */
  static <T> void diffInPlace(
      Map<Attributes, T> result, Map<Attributes, T> toDiff, Aggregator<T> aggregator) {
    toDiff.forEach(
        (k, v) -> {
          result.compute(k, (k2, v2) -> (v2 != null) ? aggregator.diff(v2, v) : v);
        });
  }
}
