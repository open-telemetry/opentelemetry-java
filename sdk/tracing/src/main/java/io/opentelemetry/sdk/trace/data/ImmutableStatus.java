/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.StatusCanonicalCode;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Defines the status of a {@link Span} by providing a standard {@link StatusCanonicalCode} in
 * conjunction with an optional descriptive message. Instances of {@code Status} are created by
 * starting with the template for the appropriate {@link StatusCanonicalCode} and supplementing it
 * with additional information: {@code Status.NOT_FOUND.withDescription("Could not find
 * 'important_file.txt'");}
 */
@AutoValue
@Immutable
public abstract class ImmutableStatus implements SpanData.Status {
  /**
   * The operation has been validated by an Application developers or Operator to have completed
   * successfully.
   */
  public static final SpanData.Status OK = create(StatusCanonicalCode.OK, null);

  /** The default status. */
  public static final SpanData.Status UNSET = create(StatusCanonicalCode.UNSET, null);

  /** The operation contains an error. */
  public static final SpanData.Status ERROR = create(StatusCanonicalCode.ERROR, null);

  /**
   * Creates a derived instance of {@code Status} with the given description.
   *
   * @param description the new description of the {@code Status}.
   * @return The newly created {@code Status} with the given description.
   * @since 0.1.0
   */
  public static SpanData.Status create(
      StatusCanonicalCode canonicalCode, @Nullable String description) {
    return new AutoValue_ImmutableStatus(canonicalCode, description);
  }
}
