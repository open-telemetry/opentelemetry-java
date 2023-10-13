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
   */
  REUSABLE_DATA,

  /**
   * Uses immutable data structures.
   *
   * <p>In this mode, the SDK passes immutable objects to exporters / readers, increasing
   * allocations but ensuring safe concurrent exports.
   */
  IMMUTABLE_DATA
}
