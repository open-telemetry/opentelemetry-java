/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Represents profiles data shared across the entire message being sent.
 *
 * @see "profiles.proto::ProfilesDictionary"
 */
@Immutable
public interface ProfileDictionaryData {

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
