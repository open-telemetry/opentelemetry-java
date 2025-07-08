/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Each Sample records values encountered in some program context. The program context is typically
 * a stack trace, perhaps augmented with auxiliary information like the thread-id, some indicator of
 * a higher level request being handled etc.
 *
 * @see "profiles.proto::Sample"
 */
@Immutable
public interface SampleData {

  /**
   * locationsStartIndex along with locationsLength refers to a slice of locations in
   * Profile.location. Supersedes locationIndices.
   */
  int getLocationsStartIndex();

  /**
   * locationsLength along with locationsStartIndex refers to a slice of locations in
   * Profile.location. locationIndices.
   */
  int getLocationsLength();

  /**
   * The type and unit of each value is defined by the corresponding entry in Profile.sample_type.
   */
  List<Long> getValues();

  /** References to attributes in Profile.attribute_table. */
  List<Integer> getAttributeIndices();

  /** Reference to link in Profile.link_table. */
  Integer getLinkIndex();

  /**
   * Timestamps associated with Sample represented in ms. These timestamps are expected to fall
   * within the Profile's time range.
   */
  List<Long> getTimestamps();
}
