/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profiles.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A ProfileContainer represents a single profile. It wraps pprof profile with OpenTelemetry
 * specific metadata.
 *
 * @see "profiles.proto::ProfileContainer"
 */
@Immutable
public interface ProfileContainerData {

  /** Returns the resource of this profile. */
  Resource getResource();

  /** Returns the instrumentation scope that generated this profile. */
  InstrumentationScopeInfo getInstrumentationScopeInfo();

  /**
   * Returns a globally unique identifier for a profile. The ID is a 16-byte array. An ID with all
   * zeroes is considered invalid. This field is required.
   */
  @SuppressWarnings("mutable")
  byte[] getProfileId();

  /**
   * Returns the start time of the profile. Value is UNIX Epoch time in nanoseconds since 00:00:00
   * UTC on 1 January 1970. This field is semantically required and it is expected that end_time >=
   * start_time.
   */
  long getStartTimeUnixNano();

  /**
   * Returns the end time of the profile. Value is UNIX Epoch time in nanoseconds since 00:00:00 UTC
   * on 1 January 1970. This field is semantically required and it is expected that end_time >=
   * start_time.
   */
  long getEndTimeUnixNano();

  /**
   * Returns profile-wide attributes. Attribute keys MUST be unique (it is not allowed to have more
   * than one attribute with the same key).
   *
   * @see
   *     "https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/common/README.md#attribute"
   */
  Attributes getAttributes();

  /**
   * Returns the number of attributes that were discarded. Attributes can be discarded because their
   * keys are too long, or because there are too many attributes. If this value is 0, then no
   * attributes were dropped.
   */
  int getDroppedAttributesCount();

  /**
   * Returns the format of the original payload. Common values are defined in semantic conventions.
   * [required if original_payload is present]
   */
  @Nullable
  String getOriginalPayloadFormat();

  /**
   * Returns the original payload, in a profiler-native format e.g. JFR. Optional. Default behavior
   * should be to not include the original payload. If the original payload is in pprof format, it
   * SHOULD not be included in this field.
   */
  @SuppressWarnings("mutable")
  @Nullable
  byte[] getOriginalPayload();

  /** Returns an extended pprof profile. Required, even when originalPayload is also present. */
  ProfileData getProfile();
}
