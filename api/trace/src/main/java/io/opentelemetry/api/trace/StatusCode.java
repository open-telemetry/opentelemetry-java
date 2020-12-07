/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

/**
 * The set of canonical status codes. If new codes are added over time they must choose a numerical
 * value that does not collide with any previously used value.
 */
public enum StatusCode {
  /** The default status. */
  UNSET,

  /**
   * The operation has been validated by an Application developers or Operator to have completed
   * successfully.
   */
  OK,

  /** The operation contains an error. */
  ERROR
}
