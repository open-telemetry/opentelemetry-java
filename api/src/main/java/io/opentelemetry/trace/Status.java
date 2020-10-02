/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.trace;

import io.opentelemetry.internal.Utils;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Defines the status of a {@link Span} by providing a standard {@link CanonicalCode} in conjunction
 * with an optional descriptive message. Instances of {@code Status} are created by starting with
 * the template for the appropriate {@link Status.CanonicalCode} and supplementing it with
 * additional information: {@code Status.NOT_FOUND.withDescription("Could not find
 * 'important_file.txt'");}
 *
 * @since 0.1.0
 */
@Immutable
public final class Status {
  /**
   * The set of canonical status codes. If new codes are added over time they must choose a
   * numerical value that does not collide with any previously used value.
   *
   * @since 0.1.0
   */
  public enum CanonicalCode {

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

    CanonicalCode(int value) {
      this.value = value;
    }

    /**
     * Returns the numerical value of the code.
     *
     * @return the numerical value of the code.
     * @since 0.1.0
     */
    public int value() {
      return value;
    }

    /**
     * Returns the status that has the current {@code CanonicalCode}..
     *
     * @return the status that has the current {@code CanonicalCode}.
     * @since 0.1.0
     */
    public Status toStatus() {
      return STATUSES_BY_VALUE.get(value);
    }
  }

  // Create the canonical list of Status instances indexed by their code values.
  private static final Map<Integer, Status> STATUSES_BY_VALUE = buildStatusList();

  private static Map<Integer, Status> buildStatusList() {
    TreeMap<Integer, Status> canonicalizer = new TreeMap<>();
    for (CanonicalCode code : CanonicalCode.values()) {
      Status replaced = canonicalizer.put(code.value(), new Status(code, null));
      if (replaced != null) {
        throw new IllegalStateException(
            "Code value duplication between "
                + replaced.getCanonicalCode().name()
                + " & "
                + code.name());
      }
    }
    return Collections.unmodifiableMap(canonicalizer);
  }

  // A pseudo-enum of Status instances mapped 1:1 with values in CanonicalCode. This simplifies
  // construction patterns for derived instances of Status.
  /**
   * The operation has been validated by an Application developers or Operator to have completed
   * successfully.
   */
  public static final Status OK = CanonicalCode.OK.toStatus();

  /** The default status. */
  public static final Status UNSET = CanonicalCode.UNSET.toStatus();

  /** The operation contains an error. */
  public static final Status ERROR = CanonicalCode.ERROR.toStatus();

  // The canonical code of this message.
  private final CanonicalCode canonicalCode;

  // An additional error message.
  @Nullable private final String description;

  private Status(CanonicalCode canonicalCode, @Nullable String description) {
    this.canonicalCode = Utils.checkNotNull(canonicalCode, "canonicalCode");
    this.description = description;
  }

  /**
   * Creates a derived instance of {@code Status} with the given description.
   *
   * @param description the new description of the {@code Status}.
   * @return The newly created {@code Status} with the given description.
   * @since 0.1.0
   */
  public Status withDescription(@Nullable String description) {
    if (Objects.equals(this.description, description)) {
      return this;
    }
    return new Status(this.canonicalCode, description);
  }

  /**
   * Returns the canonical status code.
   *
   * @return the canonical status code.
   * @since 0.1.0
   */
  public CanonicalCode getCanonicalCode() {
    return canonicalCode;
  }

  /**
   * Returns the description of this {@code Status} for human consumption.
   *
   * @return the description of this {@code Status}.
   * @since 0.1.0
   */
  @Nullable
  public String getDescription() {
    return description;
  }

  /**
   * Returns {@code true} if this {@code Status} is UNSET, i.e., not an error.
   *
   * @return {@code true} if this {@code Status} is UNSET.
   */
  public boolean isUnset() {
    return CanonicalCode.UNSET == canonicalCode;
  }

  /**
   * Returns {@code true} if this {@code Status} is ok, i.e., status is not set, or has been
   * overridden to be ok by an operator.
   *
   * @return {@code true} if this {@code Status} is OK or UNSET.
   */
  public boolean isOk() {
    return isUnset() || CanonicalCode.OK == canonicalCode;
  }

  /**
   * Equality on Statuses is not well defined. Instead, do comparison based on their CanonicalCode
   * with {@link #getCanonicalCode}. The description of the Status is unlikely to be stable, and
   * additional fields may be added to Status in the future.
   */
  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof Status)) {
      return false;
    }

    Status that = (Status) obj;
    return canonicalCode == that.canonicalCode && Objects.equals(description, that.description);
  }

  /**
   * Hash codes on Statuses are not well defined.
   *
   * @see #equals
   */
  @Override
  public int hashCode() {
    return Arrays.hashCode(new Object[] {canonicalCode, description});
  }

  @Override
  public String toString() {
    return "Status{canonicalCode=" + canonicalCode + ", description=" + description + "}";
  }
}
