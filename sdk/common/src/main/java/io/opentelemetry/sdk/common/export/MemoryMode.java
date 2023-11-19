/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.export;

/**
 * The memory semantics of the SDK.
 *
 * @since 1.31.0
 */
public enum MemoryMode {

  /**
   * Reuses objects to reduce allocations.
   *
   * <p>In this mode, the SDK reuses objects to reduce allocations, at the expense of disallowing
   * concurrent collections / exports.
   *
   * <p>Metric Signal: For DELTA aggregation temporality, the memory used for
   * recording and aggregating Attributes metric values is kept between MetricReader collect
   * operation, to avoid memory allocations. In the case of DELTA, when the configured maximum
   * cardinality of Attributes is reached, unused Attributes are cleared from this memory, during
   * collect operation, at the cost of memory allocations that will be done next time those
   * attributes are used. Those allocations can be minimized by increasing the configured
   * max cardinality.
   * For example, a user has recorded values for 1000 unique Attributes, while
   * the max cardinality configured was 2000. If after a collections only 100 unique Attributes
   * values are recorded, the MetricReader's collect operation would return 100 points, while in
   * memory the Attributes data structure keeps 1000 unique Attributes. If a user recorded values
   * for 3000 unique attributes, the values for the first 1999 Attributes would be recorded, and the
   * rest of 1001 unique Attributes values would be recorded in the CARDINALITY_OVERFLOW Attributes.
   * If after several collect operations, the user now records values to only 500 unique attributes,
   * during collect operation, the unused 1500 Attributes memory would be cleared from memory.
   */
  REUSABLE_DATA,

  /**
   * Uses immutable data structures.
   *
   * <p>In this mode, the SDK passes immutable objects to exporters / readers, increasing
   * allocations but ensuring safe concurrent exports.
   *
   * <p>Metric Signal: In DELTA aggregation temporality, the memory used for recording and
   * aggregating Attributes values is cleared during a MetricReader collect operation.
   */
  IMMUTABLE_DATA
}
