/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import com.google.auto.value.AutoValue;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
abstract class ArrayBasedTraceState implements TraceState {

  @Override
  @Nullable
  public String get(String key) {
    List<String> entries = getEntries();
    for (int i = 0; i < entries.size(); i += 2) {
      if (entries.get(i).equals(key)) {
        return entries.get(i + 1);
      }
    }
    return null;
  }

  @Override
  public int size() {
    return getEntries().size() / 2;
  }

  @Override
  public boolean isEmpty() {
    return getEntries().isEmpty();
  }

  @Override
  public void forEach(BiConsumer<String, String> consumer) {
    List<String> entries = getEntries();
    for (int i = 0; i < entries.size(); i += 2) {
      consumer.accept(entries.get(i), entries.get(i + 1));
    }
  }

  abstract List<String> getEntries();

  @Override
  public TraceStateBuilder toBuilder() {
    return new TraceStateBuilder(this);
  }

  static ArrayBasedTraceState create(List<String> entries) {
    return new AutoValue_ArrayBasedTraceState(Collections.unmodifiableList(entries));
  }

  ArrayBasedTraceState() {}
}
