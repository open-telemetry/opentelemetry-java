/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profiles.data;

import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Each Sample records values encountered in some program context. The program context is typically
 * a stack trace, perhaps augmented with auxiliary information like the thread-id, some indicator of
 * a higher level request being handled etc.
 *
 * <p>A Sample's identity (i.e. 'primary key') is the tuple of {stack_index,
 * set_of(attribute_indices), link_index}
 *
 * <p>The values and timestamps are per-observation data sharing the same context key, i.e. forming
 * a series.
 *
 * @see "profiles.proto::Sample"
 */
@Immutable
public interface SampleData {

  /**
   * Returns a new SampleData representing the given program context.
   *
   * @return a new SampleData representing the given program context.
   */
  @SuppressWarnings("AutoValueSubclassLeaked")
  static SampleData create(
      int stackIndex,
      List<Integer> attributeIndices,
      int linkIndex,
      List<Long> values,
      List<Long> timestamps) {
    return new AutoValue_ImmutableSampleData(
        stackIndex, attributeIndices, linkIndex, values, timestamps);
  }

  /** Reference to stack in Profile.stack_table. */
  int getStackIndex();

  /** References to attributes in Profile.attribute_table. */
  List<Integer> getAttributeIndices();

  /** Reference to link in Profile.link_table. 0 if none. */
  int getLinkIndex();

  /**
   * The type and unit of each value is defined by the corresponding entry in Profile.sample_type.
   */
  List<Long> getValues();

  /**
   * Timestamps associated with Sample represented in ms. These timestamps are expected to fall
   * within the Profile's time range.
   */
  List<Long> getTimestamps();
}
