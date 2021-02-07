/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import java.util.ArrayList;
import java.util.List;

class ArrayBackedLabelsBuilder implements LabelsBuilder {
  private final List<Object> data;

  ArrayBackedLabelsBuilder() {
    data = new ArrayList<>();
  }

  ArrayBackedLabelsBuilder(List<Object> data) {
    this.data = new ArrayList<>(data);
  }

  @Override
  public Labels build() {
    return ArrayBackedLabels.sortAndFilterToLabels(data.toArray());
  }

  @Override
  public LabelsBuilder put(String key, String value) {
    data.add(key);
    data.add(value);
    return this;
  }
}
