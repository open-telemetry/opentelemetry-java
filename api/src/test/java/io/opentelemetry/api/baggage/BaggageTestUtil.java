/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class BaggageTestUtil {

  static Baggage listToBaggage(Entry... entries) {
    return listToBaggage(Arrays.asList(entries));
  }

  static Baggage listToBaggage(List<Entry> entries) {
    Baggage.Builder builder = Baggage.builder();
    for (Entry entry : entries) {
      builder.put(entry.getKey(), entry.getValue(), entry.getEntryMetadata());
    }
    return builder.build();
  }

  static List<Entry> baggageToList(Baggage baggage) {
    List<Entry> list = new ArrayList<>(baggage.size());
    baggage.forEach((key, value, metadata) -> list.add(Entry.create(key, value, metadata)));
    return list;
  }

  private BaggageTestUtil() {}
}
