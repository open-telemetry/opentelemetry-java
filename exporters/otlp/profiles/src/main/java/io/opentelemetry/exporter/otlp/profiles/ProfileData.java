/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.OtelEncodingUtils;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.nio.ByteBuffer;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Represents a complete profile, including sample types, samples, mappings to binaries, locations,
 * functions, string table, and additional metadata.
 *
 * @see "profiles.proto::Profile"
 */
@Immutable
public interface ProfileData {

  /** Returns the resource of this profile. */
  Resource getResource();

  /** Returns the instrumentation scope that generated this profile. */
  InstrumentationScopeInfo getInstrumentationScopeInfo();

  /** A description of the samples associated with each Sample.value. */
  List<ValueTypeData> getSampleTypes();

  /** The set of samples recorded in this profile. */
  List<SampleData> getSamples();

  /**
   * Mapping from address ranges to the image/binary/library mapped into that address range.
   * mapping[0] will be the main binary.
   */
  List<MappingData> getMappingTable();

  /** Locations referenced by samples via location_indices. */
  List<LocationData> getLocationTable();

  /** Array of locations referenced by samples. */
  List<Integer> getLocationIndices();

  /** Functions referenced by locations. */
  List<FunctionData> getFunctionTable();

  /** Lookup table for attributes. */
  Attributes getAttributeTable();

  /** Represents a mapping between Attribute Keys and Units. */
  List<AttributeUnitData> getAttributeUnits();

  /** Lookup table for links. */
  List<LinkData> getLinkTable();

  /**
   * A common table for strings referenced by various messages. string_table[0] must always be "".
   */
  List<String> getStringTable();

  /** Time of collection (UTC) represented as nanoseconds past the epoch. */
  long getTimeNanos();

  /** Duration of the profile, if a duration makes sense. */
  long getDurationNanos();

  /**
   * The kind of events between sampled occurrences. e.g [ "cpu","cycles" ] or [ "heap","bytes" ]
   */
  ValueTypeData getPeriodType();

  /** The number of events between sampled occurrences. */
  long getPeriod();

  /** Free-form text associated with the profile. Indices into string table. */
  List<Integer> getCommentStrIndices();

  /** Type of the preferred sample. Index into the string table. */
  int getDefaultSampleTypeStringIndex();

  /**
   * Returns a globally unique identifier for a profile, as 32 character lowercase hex String. An ID
   * with all zeroes is considered invalid. This field is required.
   */
  String getProfileId();

  /**
   * Returns a globally unique identifier for a profile, as a 16 bytes array. An ID with all zeroes
   * is considered invalid. This field is required.
   */
  default byte[] getProfileIdBytes() {
    return OtelEncodingUtils.bytesFromBase16(getProfileId(), 32);
  }

  /**
   * Returns indexes of profile-wide attributes, referencing to Profile.attribute_table. Attribute
   * keys MUST be unique (it is not allowed to have more than one attribute with the same key).
   *
   * @see
   *     "https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/common/README.md#attribute"
   */
  List<Integer> getAttributeIndices();

  /**
   * Returns the total number of attributes that were recorded on this profile.
   *
   * <p>This number may be larger than the number of attributes that are attached to this profile,
   * if the total number recorded was greater than the configured maximum value.
   */
  int getTotalAttributeCount();

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
  ByteBuffer getOriginalPayload();
}
