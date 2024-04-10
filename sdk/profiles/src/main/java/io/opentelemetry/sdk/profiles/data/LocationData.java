/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profiles.data;

import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Describes a function.
 * @see "pprofextended.proto::Location"
 */
@Immutable
public interface LocationData {

  /**
   * Unique nonzero id for the location.
   * Could use instruction addresses or any integer sequence as ids.
   * @deprecated retained only for pprof compatibility.
   */
  @Deprecated
  long getId();

  /**
   * The index of the corresponding profile.Mapping for this location.
   * It can be unset if the mapping is unknown or not applicable for this profile type.
   */
  long getMappingIndex();

  /**
   * The instruction address for this location, if available.
   */
  long getAddress();

  /**
   * Multiple line indicates this location has inlined functions,
   * where the last entry represents the caller into which the
   * preceding entries were inlined.
   */
  List<LineData> getLines();

  /**
   * Provides an indication that multiple symbols map to this location's
   * address, for example due to identical code folding by the linker.
   */
  boolean isFolded();

  /**
   * Type of frame (e.g. kernel, native, python, hotspot, php).
   * Index into string table.
   */
  int getTypeIndex();

  /**
   * References to attributes in Profile.attribute_table.
   */
  List<Long> getAttributes();
}
