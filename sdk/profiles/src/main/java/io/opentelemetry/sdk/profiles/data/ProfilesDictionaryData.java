/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profiles.data;

import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Represents profiles data shared across the entire message being sent.
 *
 * @see "profiles.proto::ProfilesDictionary"
 */
@Immutable
public interface ProfilesDictionaryData {

  /**
   * Returns a new ProfileData representing the given data.
   *
   * @return a new ProfileData representing the given data.
   */
  @SuppressWarnings({"TooManyParameters", "AutoValueSubclassLeaked"})
  static ProfilesDictionaryData create(
      List<MappingData> mappingTable,
      List<LocationData> locationTable,
      List<FunctionData> functionTable,
      List<LinkData> linkTable,
      List<String> stringTable,
      List<KeyValueAndUnitData> attributeTable,
      List<StackData> stackTable) {
    return new AutoValue_ImmutableProfilesDictionaryData(
        mappingTable,
        locationTable,
        functionTable,
        linkTable,
        stringTable,
        attributeTable,
        stackTable);
  }

  /**
   * Mapping from address ranges to the image/binary/library mapped into that address range.
   * mapping[0] will be the main binary.
   */
  List<MappingData> getMappingTable();

  /** Locations referenced by samples via location_indices. */
  List<LocationData> getLocationTable();

  /** Functions referenced by locations. */
  List<FunctionData> getFunctionTable();

  /** Lookup table for links. */
  List<LinkData> getLinkTable();

  /**
   * A common table for strings referenced by various messages. string_table[0] must always be "".
   */
  List<String> getStringTable();

  /** Lookup table for attributes. */
  List<KeyValueAndUnitData> getAttributeTable();

  /** Lookup table for stacks. */
  List<StackData> getStackTable();
}
