/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.sdk.contrib.trace.testbed.multipleextractions;

import static com.google.common.truth.Truth.assertThat;

import io.grpc.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.DefaultContextPropagators;
import io.opentelemetry.context.propagation.HttpTextFormat.Getter;
import io.opentelemetry.context.propagation.HttpTextFormat.Setter;
import io.opentelemetry.contrib.trace.propagation.B3Propagator;
import io.opentelemetry.contrib.trace.propagation.StackPropagator;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import io.opentelemetry.trace.TracingContextUtils;
import io.opentelemetry.trace.propagation.HttpTraceContext;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/** This test shows extraction with multiple formats. */
public class MultipleExtractionsTest {
  @Test
  public void test() {
    HttpTraceContext traceContextPropagator = new HttpTraceContext();

    // 1. Inject a SpanContext using the TraceContext propagator.
    Map<String, String> carrier = new HashMap<>();
    Span span =
        DefaultSpan.create(
            SpanContext.create(
                new TraceId(12345, 56789),
                new SpanId(12345),
                TraceFlags.getDefault(),
                TraceState.getDefault()));
    try (Scope scope = TracingContextUtils.currentContextWith(span)) {
      traceContextPropagator.inject(Context.current(), carrier, new MapSetter());
    }

    // 2. Extract using a stack propagator that supports multiple formats,
    //    stopping ONCE a valid SpanContext could be extracted successfully.
    //    Make sure the TraceContext propagator is not the last.
    ContextPropagators propagators =
        DefaultContextPropagators.builder()
            .addHttpTextFormat(
                new StackPropagator(
                    B3Propagator.getMultipleHeaderPropagator(),
                    traceContextPropagator,
                    B3Propagator.getSingleHeaderPropagator()))
            .build();
    Context context =
        propagators.getHttpTextFormat().extract(Context.current(), carrier, new MapGetter());

    SpanContext extractedSpanContext = TracingContextUtils.getSpan(context).getContext();
    assertThat(extractedSpanContext.getTraceId()).isEqualTo(span.getContext().getTraceId());
    assertThat(extractedSpanContext.getSpanId()).isEqualTo(span.getContext().getSpanId());
  }

  private static class MapGetter implements Getter<Map<String, String>> {
    @Override
    public String get(Map<String, String> carrier, String key) {
      return carrier.get(key);
    }
  }

  private static class MapSetter implements Setter<Map<String, String>> {
    @Override
    public void set(Map<String, String> carrier, String key, String value) {
      carrier.put(key, value);
    }
  }
}
