/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.baggage;

import io.opentelemetry.baggage.Entry;
import java.util.Arrays;
import java.util.List;

class BaggageTestUtil {

  static BaggageSdk listToBaggage(Entry... entries) {
    return listToBaggage(Arrays.asList(entries));
  }

  static BaggageSdk listToBaggage(List<Entry> entries) {
    BaggageSdk.Builder builder = new BaggageSdk.Builder();
    for (Entry entry : entries) {
      builder.put(entry.getKey(), entry.getValue(), entry.getEntryMetadata());
    }
    return builder.build();
  }

  private BaggageTestUtil() {}
}
