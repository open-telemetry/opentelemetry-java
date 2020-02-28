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

import static com.google.common.truth.Truth.assertThat;

import io.grpc.Context;
import io.grpc.override.ContextStorageListener;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import io.opentelemetry.trace.unsafe.ContextUtils;
import org.apache.log4j.MDC;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link ContextStorageListener}. */
@RunWith(JUnit4.class)
public class Log4JMdcContextStorageListenerTest {
  private static final SpanContext SPAN_CONTEXT_1 =
      SpanContext.create(
          TraceId.fromLowerBase16("12345678876543211234567887654321", 0),
          SpanId.fromLowerBase16("5678432156784321", 0),
          TraceFlags.fromLowerBase16("01", 0),
          TraceState.getDefault());

  private static final SpanContext SPAN_CONTEXT_2 =
      SpanContext.create(
          TraceId.fromLowerBase16("87654321123456788765432112345678", 0),
          SpanId.fromLowerBase16("8765123487651234", 0),
          TraceFlags.fromLowerBase16("01", 0),
          TraceState.getDefault());

  Log4JMdcContextStorageListener log4jMdcInterceptor = new Log4JMdcContextStorageListener();

  @Test
  public void testAttachDetach() {
    assertThat(MDC.get(Log4JMdcContextStorageListener.TRACE_ID_CONTEXT_KEY)).isNull();
    assertThat(MDC.get(Log4JMdcContextStorageListener.SPAN_ID_CONTEXT_KEY)).isNull();

    Context context = ContextUtils.withValue(DefaultSpan.create(SPAN_CONTEXT_1));
    Context prev = context.attach();
    try {
      assertThat(MDC.get(Log4JMdcContextStorageListener.TRACE_ID_CONTEXT_KEY))
          .isEqualTo("12345678876543211234567887654321");
      assertThat(MDC.get(Log4JMdcContextStorageListener.SPAN_ID_CONTEXT_KEY))
          .isEqualTo("5678432156784321");

      Context newContext = ContextUtils.withValue(DefaultSpan.create(SPAN_CONTEXT_2));
      Context newPrev = newContext.attach();
      try {
        assertThat(MDC.get(Log4JMdcContextStorageListener.TRACE_ID_CONTEXT_KEY))
            .isEqualTo("87654321123456788765432112345678");
        assertThat(MDC.get(Log4JMdcContextStorageListener.SPAN_ID_CONTEXT_KEY))
            .isEqualTo("8765123487651234");
      } finally {
        newContext.detach(newPrev);
      }

      assertThat(MDC.get(Log4JMdcContextStorageListener.TRACE_ID_CONTEXT_KEY))
          .isEqualTo("12345678876543211234567887654321");
      assertThat(MDC.get(Log4JMdcContextStorageListener.SPAN_ID_CONTEXT_KEY))
          .isEqualTo("5678432156784321");
    } finally {
      context.detach(prev);
    }

    assertThat(MDC.get(Log4JMdcContextStorageListener.TRACE_ID_CONTEXT_KEY)).isNull();
    assertThat(MDC.get(Log4JMdcContextStorageListener.SPAN_ID_CONTEXT_KEY)).isNull();
  }
}
