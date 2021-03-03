/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.data;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import javax.annotation.concurrent.Immutable;

/**
 * Defines the status of a {@link Span} by providing a standard {@link StatusCode} in conjunction
 * with an optional descriptive message.
 */
@Immutable
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
  static StatusData create(StatusCode code, String description) {
    return ImmutableStatusData.create(code, description);
  }

  /** Returns the status code. */
  StatusCode getStatusCode();

  /**
   * Returns the description of this {@code Status} for human consumption.
   *
   * @return the description of this {@code Status}.
   */
  String getDescription();
}
