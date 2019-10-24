/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.trace;

import io.opentelemetry.internal.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
     * The operation completed successfully.
     *
     * @since 0.1.0
     */
    OK(0),

    /**
     * The operation was cancelled (typically by the caller).
     *
     * @since 0.1.0
     */
    CANCELLED(1),

    /**
     * Unknown error. An example of where this error may be returned is if a Status value received
     * from another address space belongs to an error-space that is not known in this address space.
     * Also errors raised by APIs that do not return enough error information may be converted to
     * this error.
     *
     * @since 0.1.0
     */
    UNKNOWN(2),

    /**
     * Client specified an invalid argument. Note that this differs from FAILED_PRECONDITION.
     * INVALID_ARGUMENT indicates arguments that are problematic regardless of the state of the
     * system (e.g., a malformed file name).
     *
     * @since 0.1.0
     */
    INVALID_ARGUMENT(3),

    /**
     * Deadline expired before operation could complete. For operations that change the state of the
     * system, this error may be returned even if the operation has completed successfully. For
     * example, a successful response from a server could have been delayed long enough for the
     * deadline to expire.
     *
     * @since 0.1.0
     */
    DEADLINE_EXCEEDED(4),

    /**
     * Some requested entity (e.g., file or directory) was not found.
     *
     * @since 0.1.0
     */
    NOT_FOUND(5),

    /**
     * Some entity that we attempted to create (e.g., file or directory) already exists.
     *
     * @since 0.1.0
     */
    ALREADY_EXISTS(6),

    /**
     * The caller does not have permission to execute the specified operation. PERMISSION_DENIED
     * must not be used for rejections caused by exhausting some resource (use RESOURCE_EXHAUSTED
     * instead for those errors). PERMISSION_DENIED must not be used if the caller cannot be
     * identified (use UNAUTHENTICATED instead for those errors).
     *
     * @since 0.1.0
     */
    PERMISSION_DENIED(7),

    /**
     * Some resource has been exhausted, perhaps a per-user quota, or perhaps the entire file system
     * is out of space.
     *
     * @since 0.1.0
     */
    RESOURCE_EXHAUSTED(8),

    /**
     * Operation was rejected because the system is not in a state required for the operation's
     * execution. For example, directory to be deleted may be non-empty, an rmdir operation is
     * applied to a non-directory, etc.
     *
     * <p>A litmus test that may help a service implementor in deciding between FAILED_PRECONDITION,
     * ABORTED, and UNAVAILABLE: (a) Use UNAVAILABLE if the client can retry just the failing call.
     * (b) Use ABORTED if the client should retry at a higher-level (e.g., restarting a
     * read-modify-write sequence). (c) Use FAILED_PRECONDITION if the client should not retry until
     * the system state has been explicitly fixed. E.g., if an "rmdir" fails because the directory
     * is non-empty, FAILED_PRECONDITION should be returned since the client should not retry unless
     * they have first fixed up the directory by deleting files from it.
     *
     * @since 0.1.0
     */
    FAILED_PRECONDITION(9),

    /**
     * The operation was aborted, typically due to a concurrency issue like sequencer check
     * failures, transaction aborts, etc.
     *
     * <p>See litmus test above for deciding between FAILED_PRECONDITION, ABORTED, and UNAVAILABLE.
     *
     * @since 0.1.0
     */
    ABORTED(10),

    /**
     * Operation was attempted past the valid range. E.g., seeking or reading past end of file.
     *
     * <p>Unlike INVALID_ARGUMENT, this error indicates a problem that may be fixed if the system
     * state changes. For example, a 32-bit file system will generate INVALID_ARGUMENT if asked to
     * read at an offset that is not in the range [0,2^32-1], but it will generate OUT_OF_RANGE if
     * asked to read from an offset past the current file size.
     *
     * <p>There is a fair bit of overlap between FAILED_PRECONDITION and OUT_OF_RANGE. We recommend
     * using OUT_OF_RANGE (the more specific error) when it applies so that callers who are
     * iterating through a space can easily look for an OUT_OF_RANGE error to detect when they are
     * done.
     *
     * @since 0.1.0
     */
    OUT_OF_RANGE(11),

    /**
     * Operation is not implemented or not supported/enabled in this service.
     *
     * @since 0.1.0
     */
    UNIMPLEMENTED(12),

    /**
     * Internal errors. Means some invariants expected by underlying system has been broken. If you
     * see one of these errors, something is very broken.
     *
     * @since 0.1.0
     */
    INTERNAL(13),

    /**
     * The service is currently unavailable. This is a most likely a transient condition and may be
     * corrected by retrying with a backoff.
     *
     * <p>See litmus test above for deciding between FAILED_PRECONDITION, ABORTED, and UNAVAILABLE.
     *
     * @since 0.1.0
     */
    UNAVAILABLE(14),

    /**
     * Unrecoverable data loss or corruption.
     *
     * @since 0.1.0
     */
    DATA_LOSS(15),

    /**
     * The request does not have valid authentication credentials for the operation.
     *
     * @since 0.1.0
     */
    UNAUTHENTICATED(16);

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
      return STATUS_LIST.get(value);
    }
  }

  // Create the canonical list of Status instances indexed by their code values.
  private static final List<Status> STATUS_LIST = buildStatusList();

  private static List<Status> buildStatusList() {
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
    return Collections.unmodifiableList(new ArrayList<>(canonicalizer.values()));
  }

  // A pseudo-enum of Status instances mapped 1:1 with values in CanonicalCode. This simplifies
  // construction patterns for derived instances of Status.
  /**
   * The operation completed successfully.
   *
   * @since 0.1.0
   */
  public static final Status OK = CanonicalCode.OK.toStatus();

  /**
   * The operation was cancelled (typically by the caller).
   *
   * @since 0.1.0
   */
  public static final Status CANCELLED = CanonicalCode.CANCELLED.toStatus();

  /**
   * Unknown error. See {@link CanonicalCode#UNKNOWN}.
   *
   * @since 0.1.0
   */
  public static final Status UNKNOWN = CanonicalCode.UNKNOWN.toStatus();

  /**
   * Client specified an invalid argument. See {@link CanonicalCode#INVALID_ARGUMENT}.
   *
   * @since 0.1.0
   */
  public static final Status INVALID_ARGUMENT = CanonicalCode.INVALID_ARGUMENT.toStatus();

  /**
   * Deadline expired before operation could complete. See {@link CanonicalCode#DEADLINE_EXCEEDED}.
   *
   * @since 0.1.0
   */
  public static final Status DEADLINE_EXCEEDED = CanonicalCode.DEADLINE_EXCEEDED.toStatus();

  /**
   * Some requested entity (e.g., file or directory) was not found.
   *
   * @since 0.1.0
   */
  public static final Status NOT_FOUND = CanonicalCode.NOT_FOUND.toStatus();

  /**
   * Some entity that we attempted to create (e.g., file or directory) already exists.
   *
   * @since 0.1.0
   */
  public static final Status ALREADY_EXISTS = CanonicalCode.ALREADY_EXISTS.toStatus();

  /**
   * The caller does not have permission to execute the specified operation. See {@link
   * CanonicalCode#PERMISSION_DENIED}.
   *
   * @since 0.1.0
   */
  public static final Status PERMISSION_DENIED = CanonicalCode.PERMISSION_DENIED.toStatus();

  /**
   * The request does not have valid authentication credentials for the operation.
   *
   * @since 0.1.0
   */
  public static final Status UNAUTHENTICATED = CanonicalCode.UNAUTHENTICATED.toStatus();

  /**
   * Some resource has been exhausted, perhaps a per-user quota, or perhaps the entire file system
   * is out of space.
   *
   * @since 0.1.0
   */
  public static final Status RESOURCE_EXHAUSTED = CanonicalCode.RESOURCE_EXHAUSTED.toStatus();

  /**
   * Operation was rejected because the system is not in a state required for the operation's
   * execution. See {@link CanonicalCode#FAILED_PRECONDITION}.
   *
   * @since 0.1.0
   */
  public static final Status FAILED_PRECONDITION = CanonicalCode.FAILED_PRECONDITION.toStatus();

  /**
   * The operation was aborted, typically due to a concurrency issue like sequencer check failures,
   * transaction aborts, etc. See {@link CanonicalCode#ABORTED}.
   *
   * @since 0.1.0
   */
  public static final Status ABORTED = CanonicalCode.ABORTED.toStatus();

  /**
   * Operation was attempted past the valid range. See {@link CanonicalCode#OUT_OF_RANGE}.
   *
   * @since 0.1.0
   */
  public static final Status OUT_OF_RANGE = CanonicalCode.OUT_OF_RANGE.toStatus();

  /**
   * Operation is not implemented or not supported/enabled in this service.
   *
   * @since 0.1.0
   */
  public static final Status UNIMPLEMENTED = CanonicalCode.UNIMPLEMENTED.toStatus();

  /**
   * Internal errors. See {@link CanonicalCode#INTERNAL}.
   *
   * @since 0.1.0
   */
  public static final Status INTERNAL = CanonicalCode.INTERNAL.toStatus();

  /**
   * The service is currently unavailable. See {@link CanonicalCode#UNAVAILABLE}.
   *
   * @since 0.1.0
   */
  public static final Status UNAVAILABLE = CanonicalCode.UNAVAILABLE.toStatus();

  /**
   * Unrecoverable data loss or corruption.
   *
   * @since 0.1.0
   */
  public static final Status DATA_LOSS = CanonicalCode.DATA_LOSS.toStatus();

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
    if (Utils.equalsObjects(this.description, description)) {
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
   * Returns {@code true} if this {@code Status} is OK, i.e., not an error.
   *
   * @return {@code true} if this {@code Status} is OK.
   * @since 0.1.0
   */
  public boolean isOk() {
    return CanonicalCode.OK == canonicalCode;
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
    return canonicalCode == that.canonicalCode
        && Utils.equalsObjects(description, that.description);
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
