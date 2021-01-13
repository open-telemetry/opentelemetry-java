/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.data;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import javax.annotation.Nullable;

/**
 * Defines the status of a {@link Span} by providing a standard {@link StatusCode} in conjunction
 * with an optional descriptive message.
 */
public interface StatusData {

  /**
   * Returns a {@link StatusData} indicating the operation has been validated by an application
   * developer or operator to have completed successfully.
   */
  static StatusData ok() {
    return ImmutableStatusData.OK;
  }

  /** Returns the default {@link StatusData}. */
  static StatusData unset() {
    return ImmutableStatusData.UNSET;
  }

  /** Returns a {@link StatusData} indicating an error occurred. */
  static StatusData error() {
    return ImmutableStatusData.ERROR;
  }

  /**
   * Returns a {@link StatusData} with the given {@code code} and {@code description}. If {@code
   * description} is {@code null}, the returned {@link StatusData} does not have a description.
   */
  static StatusData create(StatusCode code, @Nullable String description) {
    return ImmutableStatusData.create(code, description);
  }

  /** Returns the status code. */
  StatusCode getStatusCode();

  /**
   * Returns the description of this {@code Status} for human consumption.
   *
   * @return the description of this {@code Status}.
   */
  @Nullable
  String getDescription();

  /**
   * Returns {@code true} if this {@code Status} is UNSET, i.e., not an error.
   *
   * @return {@code true} if this {@code Status} is UNSET.
   * @deprecated Compare {@link #getStatusCode()} with {@link StatusCode#UNSET}
   */
  // TODO: Consider to remove this in a future PR. Avoid too many changes in the initial PR.
  @Deprecated
  default boolean isUnset() {
    return StatusCode.UNSET == getStatusCode();
  }

  /**
   * Returns {@code true} if this {@code Status} is ok, i.e., status is not set, or has been
   * overridden to be ok by an operator.
   *
   * @return {@code true} if this {@code Status} is OK or UNSET.
   */
  default boolean isOk() {
    switch (getStatusCode()) {
      case UNSET:
      case OK:
        return true;
      case ERROR:
      default:
        return false;
    }
  }
}
