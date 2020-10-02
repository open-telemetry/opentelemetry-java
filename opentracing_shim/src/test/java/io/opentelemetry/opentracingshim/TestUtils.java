/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import java.util.HashMap;
import java.util.Map;

final class TestUtils {
  private TestUtils() {}

  static Map<String, String> getBaggageMap(Iterable<Map.Entry<String, String>> baggage) {
    Map<String, String> baggageMap = new HashMap<>();
    for (Map.Entry<String, String> entry : baggage) {
      baggageMap.put(entry.getKey(), entry.getValue());
    }

    return baggageMap;
  }
}
