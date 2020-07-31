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

import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.TracingContextUtils;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.core.util.ContextDataProvider;

/**
 * This ContextDataProvider is loaded via the ServiceProvider facility. {@link #supplyContextData()}
 * is called when a log entry is created.
 */
public class TraceContextDataProvider implements ContextDataProvider {
  /**
   * This method is called on the creation of a log event, and is called in the same thread as the
   * call to the logger. This allows us to pull out request correlation information and make it
   * available to a layout, even if the logger is using an {@link
   * org.apache.logging.log4j.core.appender.AsyncAppender}
   *
   * @return A map containing string versions of the traceid, spanid, and traceflags which can then
   *     be accessed from layout components
   * @see OpenTelemetryJsonLayout
   */
  @Override
  public Map<String, String> supplyContextData() {
    Span span = TracingContextUtils.getCurrentSpan();
    Map<String, String> map = new HashMap<>();
    if (span != null && span.getContext().isValid()) {
      SpanContext context = span.getContext();
      if (context != null && context.isValid()) {
        map.put("traceid", span.getContext().getTraceId().toString());
        map.put("spanid", span.getContext().getSpanId().toString());
        map.put("traceflags", span.getContext().getTraceFlags().toLowerBase16());
      }
    }
    return map;
  }
}
