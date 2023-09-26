/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.export;

/** The memory semantics of the SDK. */
public enum MemoryMode {

  /**
   * Reuses objects to reduce allocations.
   *
   * <p>In this mode, the SDK reuses objects to reduce allocations, at the expense of disallowing concurrent collections / exports.
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
