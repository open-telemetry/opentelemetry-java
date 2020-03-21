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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.sdk.correlationcontext.CorrelationContextManagerSdk;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.trace.Tracer;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SpanShimTest {
  private final TracerSdkProvider tracerSdkFactory = TracerSdkProvider.builder().build();
  private final Tracer tracer = tracerSdkFactory.get("SpanShimTest");
  private final TelemetryInfo telemetryInfo =
      new TelemetryInfo(tracer, new CorrelationContextManagerSdk(), OpenTelemetry.getPropagators());
  private io.opentelemetry.trace.Span span;

  private static final String SPAN_NAME = "Span";

  @Before
  public void setUp() {
    span = telemetryInfo.tracer().spanBuilder(SPAN_NAME).startSpan();
  }

  @After
  public void tearDown() {
    span.end();
  }

  @Test
  public void context_simple() {
    SpanShim spanShim = new SpanShim(telemetryInfo, span);

    SpanContextShim contextShim = (SpanContextShim) spanShim.context();
    assertNotNull(contextShim);
    assertEquals(contextShim.getSpanContext(), span.getContext());
    assertEquals(contextShim.toTraceId(), span.getContext().getTraceId().toString());
    assertEquals(contextShim.toSpanId(), span.getContext().getSpanId().toString());
    assertFalse(contextShim.baggageItems().iterator().hasNext());
  }

  @Test
  public void baggage() {
    SpanShim spanShim = new SpanShim(telemetryInfo, span);

    spanShim.setBaggageItem("key1", "value1");
    spanShim.setBaggageItem("key2", "value2");
    assertEquals(spanShim.getBaggageItem("key1"), "value1");
    assertEquals(spanShim.getBaggageItem("key2"), "value2");

    SpanContextShim contextShim = (SpanContextShim) spanShim.context();
    assertNotNull(contextShim);
    Map<String, String> baggageMap = getBaggageMap(contextShim.baggageItems());
    assertEquals(2, baggageMap.size());
    assertEquals(baggageMap.get("key1"), "value1");
    assertEquals(baggageMap.get("key2"), "value2");
  }

  @Test
  public void baggage_replacement() {
    SpanShim spanShim = new SpanShim(telemetryInfo, span);
    SpanContextShim contextShim1 = (SpanContextShim) spanShim.context();

    spanShim.setBaggageItem("key1", "value1");
    SpanContextShim contextShim2 = (SpanContextShim) spanShim.context();
    assertNotEquals(contextShim1, contextShim2);
    assertFalse(contextShim1.baggageItems().iterator().hasNext()); /* original, empty */
    assertTrue(contextShim2.baggageItems().iterator().hasNext()); /* updated, with values */
  }

  @Test
  public void baggage_differentShimObjs() {
    SpanShim spanShim1 = new SpanShim(telemetryInfo, span);
    spanShim1.setBaggageItem("key1", "value1");

    /* Baggage should be synchronized among different SpanShim objects
     * refering to the same Span.*/
    SpanShim spanShim2 = new SpanShim(telemetryInfo, span);
    spanShim2.setBaggageItem("key1", "value2");
    assertEquals(spanShim1.getBaggageItem("key1"), "value2");
    assertEquals(spanShim2.getBaggageItem("key1"), "value2");
    assertEquals(
        getBaggageMap(spanShim1.context().baggageItems()),
        getBaggageMap(spanShim2.context().baggageItems()));
  }
}
