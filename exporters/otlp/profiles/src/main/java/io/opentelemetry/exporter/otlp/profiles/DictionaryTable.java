/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This data structure is effectively an indexed Set.
 *
 * <p>It stores each offered element without duplication (like a Set, not a List), but supports
 * reference to elements by their position (like a List index).
 *
 * <p>This class is not threadsafe and must be externally synchronized.
 *
 * <p>For a given Object o, after i = putIfAbsent(o), then getTable().get(i).equals(o);
 *
 * @param <T> the type of elements maintained by this table. The type should implement equals and
 *     hashCode in a manner consistent with Set/Map key expectations.
 */
public class DictionaryTable<T> {

  // Whilst it's possible to compute either from the other, we keep two views on the data,
  // prioritising access efficiency over memory footprint.
  private final List<T> table = new ArrayList<>();
  private final Map<T, Integer> map = new HashMap<>();

  /**
   * Stores the provided element if an equivalent is not already present, and returns its index.
   *
   * <p>Note that whilst the update semantics of this method are consistent with Map.putIfAbsent,
   * the return value is always the index, i.e. reflects the post-update state, not the prior state,
   * and therefore does not allow for determining if the method had an effect or not.
   *
   * @param value an element to store.
   * @return the index of the added or existing element.
   */
  public Integer putIfAbsent(T value) {
    Integer index = map.computeIfAbsent(value, k -> map.size());
    if (map.size() != table.size()) {
      table.add(value);
    }
    return index;
  }

  /**
   * Provides a view of the Table in List form.
   *
   * @return an immutable List containing the table entries in index position.
   */
  public List<T> getTable() {
    return Collections.unmodifiableList(table);
  }
}
