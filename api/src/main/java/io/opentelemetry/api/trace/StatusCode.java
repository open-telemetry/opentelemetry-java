/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

/** The set of canonical status codes. */
public enum StatusCode {

  /**
   * The operation has been validated by an Application developers or Operator to have completed
   * successfully.
   */
  OK,

  /** The default status. */
  UNSET,

  /** The operation contains an error. */
  ERROR;
}
