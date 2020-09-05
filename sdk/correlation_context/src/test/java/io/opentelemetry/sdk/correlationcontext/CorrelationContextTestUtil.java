/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.correlationcontext;

import io.opentelemetry.correlationcontext.Entry;
import java.util.Arrays;
import java.util.List;

class CorrelationContextTestUtil {

  static CorrelationContextSdk listToCorrelationContext(Entry... entries) {
    return listToCorrelationContext(Arrays.asList(entries));
  }

  static CorrelationContextSdk listToCorrelationContext(List<Entry> entries) {
    CorrelationContextSdk.Builder builder = new CorrelationContextSdk.Builder();
    for (Entry entry : entries) {
      builder.put(entry.getKey(), entry.getValue(), entry.getEntryMetadata());
    }
    return builder.build();
  }

  private CorrelationContextTestUtil() {}
}
