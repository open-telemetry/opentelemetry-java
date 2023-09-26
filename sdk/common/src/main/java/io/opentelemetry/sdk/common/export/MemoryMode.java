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
   * <p>In this mode, the SDK passes immutable objects to exporters / readers, increasing allocations but ensuring safe concurrent exports.
   *
   * <p>More specifically, data objects returned by the SDK are immutable.
   */
  IMMUTABLE_DATA
}
