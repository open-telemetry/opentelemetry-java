/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.oltp.data;

import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Each Sample records values encountered in some program context. The program context is typically
 * a stack trace, perhaps augmented with auxiliary information like the thread-id, some indicator of
 * a higher level request being handled etc.
 *
 * @see "pprofextended.proto::Sample"
 */
@Immutable
public interface SampleData {

  /**
   * The indices recorded here correspond to locations in Profile.location. The leaf is at
   * location_index[0].
   *
   * @deprecated superseded by locations_start_index / locations_length
   */
  @Deprecated
  List<Long> getLocationIndices();

  /**
   * locationsStartIndex along with locationsLength refers to a slice of locations in
   * Profile.location. Supersedes locationIndices.
   */
  long getLocationsStartIndex();

  /**
   * locationsLength along with locationsStartIndex refers to a slice of locations in
   * Profile.location. locationIndices.
   */
  long getLocationsLength();

  /**
   * reference to a 128bit id that uniquely identifies this stacktrace, globally. Index into the
   * string table.
   */
  int getStacktraceIdIndex();

  /**
   * The type and unit of each value is defined by the corresponding entry in Profile.sample_type.
   */
  List<Long> getValues();

  /** Additional context for this sample. It can include thread id, allocation size, etc. */
  @Deprecated
  List<LabelData> getLabels();

  /** References to attributes in Profile.attribute_table. */
  List<Long> getAttributes();

  /** Reference to link in Profile.link_table. */
  long getLink();

  /**
   * Timestamps associated with Sample represented in ms. These timestamps are expected to fall
   * within the Profile's time range.
   */
  List<Long> getTimestamps();
}
