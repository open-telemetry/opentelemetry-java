/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.opentracingshim;

import static io.opentelemetry.opentracingshim.TestUtils.getBaggageMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.sdk.correlationcontext.CorrelationContextManagerSdk;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.trace.Tracer;
import org.junit.jupiter.api.Test;

class SpanBuilderShimTest {
  private final TracerSdkProvider tracerSdkFactory = TracerSdkProvider.builder().build();
  private final Tracer tracer = tracerSdkFactory.get("SpanShimTest");
  private final TelemetryInfo telemetryInfo =
      new TelemetryInfo(tracer, new CorrelationContextManagerSdk(), OpenTelemetry.getPropagators());

  private static final String SPAN_NAME = "Span";

  @Test
  void baggage_parent() {
    SpanShim parentSpan = (SpanShim) new SpanBuilderShim(telemetryInfo, SPAN_NAME).start();
    try {
      parentSpan.setBaggageItem("key1", "value1");

      SpanShim childSpan =
          (SpanShim) new SpanBuilderShim(telemetryInfo, SPAN_NAME).asChildOf(parentSpan).start();
      try {
        assertEquals(childSpan.getBaggageItem("key1"), "value1");
        assertEquals(
            getBaggageMap(childSpan.context().baggageItems()),
            getBaggageMap(parentSpan.context().baggageItems()));
      } finally {
        childSpan.finish();
      }
    } finally {
      parentSpan.finish();
    }
  }

  @Test
  void baggage_parentContext() {
    SpanShim parentSpan = (SpanShim) new SpanBuilderShim(telemetryInfo, SPAN_NAME).start();
    try {
      parentSpan.setBaggageItem("key1", "value1");

      SpanShim childSpan =
          (SpanShim)
              new SpanBuilderShim(telemetryInfo, SPAN_NAME).asChildOf(parentSpan.context()).start();
      try {
        assertEquals(childSpan.getBaggageItem("key1"), "value1");
        assertEquals(
            getBaggageMap(childSpan.context().baggageItems()),
            getBaggageMap(parentSpan.context().baggageItems()));
      } finally {
        childSpan.finish();
      }
    } finally {
      parentSpan.finish();
    }
  }

  @Test
  void parent_NullContextShim() {
    /* SpanContextShim is null until Span.context() or Span.getBaggageItem() are called.
     * Verify a null SpanContextShim in the parent is handled properly. */
    SpanShim parentSpan = (SpanShim) new SpanBuilderShim(telemetryInfo, SPAN_NAME).start();
    try {
      SpanShim childSpan =
          (SpanShim) new SpanBuilderShim(telemetryInfo, SPAN_NAME).asChildOf(parentSpan).start();
      try {
        assertFalse(childSpan.context().baggageItems().iterator().hasNext());
      } finally {
        childSpan.finish();
      }
    } finally {
      parentSpan.finish();
    }
  }
}
