/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import java.util.ArrayList;
import java.util.List;

/** A builder of {@link Labels} supporting an arbitrary number of key-value pairs. */
public class LabelsBuilder {
  private final List<Object> data;

  LabelsBuilder() {
    data = new ArrayList<>();
  }

  LabelsBuilder(List<Object> data) {
    this.data = new ArrayList<>(data);
  }

  /** Create the {@link Labels} from this. */
  public Labels build() {
    return Labels.sortAndFilterToLabels(data.toArray());
  }

  /**
   * Puts a single label into this Builder.
   *
   * @return this Builder
   */
  public LabelsBuilder put(String key, String value) {
    data.add(key);
    data.add(value);
    return this;
  }
}
