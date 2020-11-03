/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

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

  private BaggageTestUtil() {}
}
