/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import javax.annotation.concurrent.Immutable;

/**
 * Defines the status of a {@link Span} by providing a standard {@link StatusCode} in conjunction
 * with an optional descriptive message. Instances of {@code Status} are created by starting with
 * the template for the appropriate {@link StatusCode} and supplementing it with additional
 * information: {@code Status.NOT_FOUND.withDescription("Could not find 'important_file.txt'");}
 */
@AutoValue
@Immutable
abstract class ImmutableStatusData implements StatusData {
  /**
   * The operation has been validated by an Application developers or Operator to have completed
   * successfully.
   */
  static final StatusData OK = createInternal(StatusCode.OK, "");

  /** The default status. */
  static final StatusData UNSET = createInternal(StatusCode.UNSET, "");

  /** The operation contains an error. */
  static final StatusData ERROR = createInternal(StatusCode.ERROR, "");

  /**
   * Creates a derived instance of {@code Status} with the given description.
   *
   * @param description the new description of the {@code Status}.
   * @return The newly created {@code Status} with the given description.
   */
  static StatusData create(StatusCode statusCode, String description) {
    if (description == null || description.isEmpty()) {
      switch (statusCode) {
        case UNSET:
          return StatusData.unset();
        case OK:
          return StatusData.ok();
        case ERROR:
          return StatusData.error();
      }
    }
    return createInternal(statusCode, description);
  }

  private static StatusData createInternal(StatusCode statusCode, String description) {
    return new AutoValue_ImmutableStatusData(statusCode, description);
  }
}
