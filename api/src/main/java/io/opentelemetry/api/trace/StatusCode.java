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

  /**
   * The operation has been validated by an Application developers or Operator to have completed
   * successfully.
   */
  OK(0),

  /** The default status. */
  UNSET(1),

  /** The operation contains an error. */
  ERROR(2);

  private final int value;

  StatusCode(int value) {
    this.value = value;
  }

  /**
   * Returns the numerical value of the code.
   *
   * @return the numerical value of the code.
   */
  public int value() {
    return value;
  }
}
