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

package io.opentelemetry.contrib.context.interceptor;

import io.grpc.Context;
import io.grpc.override.ContextStorageListener;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.unsafe.ContextUtils;
import org.apache.log4j.MDC;

public class Log4JMdcContextStorageListener implements ContextStorageListener.Provider {
  /**
   * Context key for the current trace ID. The name is {@value}.
   *
   * @since 0.17
   */
  public static final String TRACE_ID_CONTEXT_KEY = "traceId";

  /**
   * Context key for the current span ID. The name is {@value}.
   *
   * @since 0.17
   */
  public static final String SPAN_ID_CONTEXT_KEY = "spanId";

  @Override
  public ContextStorageListener create() {
    return new ContextStorageListener() {
      @Override
      public void contextUpdated(Context oldContext, Context newContext) {
        SpanContext current = ContextUtils.getValue(newContext).getContext();
        if (current.isValid()) {
          MDC.put(TRACE_ID_CONTEXT_KEY, current.getTraceId().toLowerBase16());
          MDC.put(SPAN_ID_CONTEXT_KEY, current.getSpanId().toLowerBase16());
        } else {
          MDC.remove(TRACE_ID_CONTEXT_KEY);
          MDC.remove(SPAN_ID_CONTEXT_KEY);
        }
      }
    };
  }
}
