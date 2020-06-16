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

package io.opentelemetry.contrib.logging.log4j2;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.Tracer;
import org.apache.logging.log4j.core.util.ContextDataProvider;
import java.util.HashMap;
import java.util.Map;

public class TraceContextDataProvider implements ContextDataProvider {
  @Override
  public Map<String, String> supplyContextData() {
    Tracer tracer = OpenTelemetry.getTracerProvider().get("ot_prototype_logging");
    Span span = tracer.getCurrentSpan();
    Map<String, String> map = new HashMap<>();
    if (span != null && span.isRecording()) {
      SpanContext context = span.getContext();
      if (context != null && context.isValid()) {
        map.put("traceid", span.getContext().getTraceId().toLowerBase16());
        map.put("spanid", span.getContext().getSpanId().toLowerBase16());
        map.put("traceflags", span.getContext().getTraceFlags().toLowerBase16());
      }
    }
    return map;
  }
}
