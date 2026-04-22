/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profiles.data;

import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Describes function and line table debug information.
 *
 * @see "profiles.proto::Location"
 */
@Immutable
public interface LocationData {

  /**
   * Returns a new LocationData describing the given function and line table information.
   *
   * @return a new LocationData describing the given function and line table information.
   */
  @SuppressWarnings("AutoValueSubclassLeaked")
  static LocationData create(
      int mappingIndex, long address, List<LineData> lines, List<Integer> attributeIndices) {
    return new AutoValue_ImmutableLocationData(mappingIndex, address, lines, attributeIndices);
  }

  /**
   * The index of the corresponding profile.Mapping for this location. It can be 0 if the mapping is
   * unknown or not applicable for this profile type.
   */
  int getMappingIndex();

  /** The instruction address for this location, if available. */
  long getAddress();

  /**
   * Multiple line indicates this location has inlined functions, where the last entry represents
   * the caller into which the preceding entries were inlined.
   */
  List<LineData> getLines();

  /** References to attributes in Profile.attribute_table. */
  List<Integer> getAttributeIndices();
}
