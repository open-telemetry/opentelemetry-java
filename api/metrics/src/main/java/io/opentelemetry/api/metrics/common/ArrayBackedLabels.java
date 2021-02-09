/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics.common;

import io.opentelemetry.api.internal.ImmutableKeyValuePairs;
import java.util.List;
import javax.annotation.concurrent.Immutable;

@Immutable
final class ArrayBackedLabels extends ImmutableKeyValuePairs<String, String> implements Labels {
  private static final Labels EMPTY = Labels.builder().build();

  static Labels empty() {
    return EMPTY;
  }

  ArrayBackedLabels(List<Object> data) {
    super(data);
  }

  static Labels sortAndFilterToLabels(Object... data) {
    return new ArrayBackedLabels(sortAndFilter(data));
  }

  @Override
  public LabelsBuilder toBuilder() {
    return new ArrayBackedLabelsBuilder(data());
  }
}
