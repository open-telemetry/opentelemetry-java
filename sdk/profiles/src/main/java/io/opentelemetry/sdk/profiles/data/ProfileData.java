/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profiles.data;

import io.opentelemetry.api.common.Attributes;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Represents a complete profile, including sample types, samples, mappings to binaries, locations,
 * functions, string table, and additional metadata.
 *
 * @see "pprofextended.proto::Profile"
 */
@Immutable
public interface ProfileData {

  /** A description of the samples associated with each Sample.value. */
  List<ValueTypeData> getSampleTypes();

  /** The set of samples recorded in this profile. */
  List<SampleData> getSamples();

  /**
   * Mapping from address ranges to the image/binary/library mapped into that address range.
   * mapping[0] will be the main binary.
   */
  List<MappingData> getMappings();

  /** Locations referenced by samples via location_indices. */
  List<LocationData> getLocations();

  /** Array of locations referenced by samples. */
  List<Long> getLocationIndices();

  /** Functions referenced by locations. */
  List<FunctionData> getFunctions();

  /** Lookup table for attributes. */
  Attributes getAttributes();

  /** Represents a mapping between Attribute Keys and Units. */
  List<AttributeUnitData> getAttributeUnits();

  /** Lookup table for links. */
  List<LinkData> getLinks();

  /**
   * A common table for strings referenced by various messages. string_table[0] must always be "".
   */
  List<String> getStringTable();

  /**
   * Frames with Function.function_name fully matching the following regexp will be dropped from the
   * samples, along with their successors. Index into string table.
   */
  long getDropFrames();

  /**
   * Frames with Function.function_name fully matching the following regexp will be kept, even if
   * matching drop_frames pattern. Index into string table.
   */
  long getKeepFrames();

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
  List<Long> getComment();

  /** Type of the preferred sample. Index into the string table. */
  long getDefaultSampleType();
}
