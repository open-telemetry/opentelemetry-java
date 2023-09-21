/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.export;

/** The type of memory allocation used during signal collection in the different readers. */
public enum MemoryMode {

  /**
   * Reuses objects to reduce garbage collection.
   *
   * <p>In this mode, the different signal readers, reuses objects to significantly reduce garbage
   * collection, at the expense of disallowing concurrent collection operations.
   *
   * <p>More specifically, data objects returned by the SDK to be used by readers or exporters are
   * reused across collection calls
   */
  REUSABLE_DATA,

  /**
   * Uses immutable data structures.
   *
   * <p>In this mode, the data collected by the readers, is immutable, meant to be used once. This
   * allows running reader's collection operations concurrently, at the expense of increased garbage
   * collection.
   *
   * <p>More specifically, data objects returned by the SDK are immutable.
   */
  IMMUTABLE_DATA
}
