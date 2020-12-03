/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.internal.ImmutableKeyValuePairs;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
abstract class ArrayBackedLabels extends ImmutableKeyValuePairs<String, String> implements Labels {
  private static final Labels EMPTY = Labels.builder().build();

  static Labels empty() {
    return EMPTY;
  }

  ArrayBackedLabels() {}

  @Override
  protected abstract List<Object> data();

  @Override
  public void forEach(BiConsumer<String, String> consumer) {
    List<Object> data = data();
    for (int i = 0; i < data.size(); i += 2) {
      consumer.accept((String) data.get(i), (String) data.get(i + 1));
    }
  }

  static Labels sortAndFilterToLabels(Object... data) {
    return new AutoValue_ArrayBackedLabels(sortAndFilter(data, /* filterNullValues= */ false));
  }

  @Override
  public LabelsBuilder toBuilder() {
    return new ArrayBackedLabelsBuilder(data());
  }
}
