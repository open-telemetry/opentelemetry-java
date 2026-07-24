/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Link;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracestate;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OpenTelemetrySpanImplTest {

  private static final String TRACE_ID = "0123456789abcdef0123456789abcdef";
  private static final String SPAN_ID = "fedcba9876543210";

  @Mock private Span otelSpan;

  private OpenTelemetrySpanImpl shimSpan() {
    when(otelSpan.getSpanContext()).thenReturn(SpanContext.getInvalid());
    return new OpenTelemetrySpanImpl(otelSpan);
  }

  @Test
  void setStatus_preservesDescription() {
    shimSpan().setStatus(Status.UNKNOWN.withDescription("connection refused"));

    verify(otelSpan).setStatus(StatusCode.ERROR, "connection refused");
  }

  @Test
  void setStatus_withoutDescription_setsCodeOnly() {
    shimSpan().setStatus(Status.OK);

    verify(otelSpan).setStatus(StatusCode.OK);
    verify(otelSpan, never()).setStatus(any(StatusCode.class), anyString());
  }

  @Test
  void addLink_delegatesToOtelSpan() {
    shimSpan().addLink(Link.fromSpanContext(ocSpanContext(), Link.Type.CHILD_LINKED_SPAN));

    verify(otelSpan).addLink(expectedOtelSpanContext(), Attributes.empty());
  }

  @Test
  void addLink_mapsAttributes() {
    shimSpan()
        .addLink(
            Link.fromSpanContext(
                ocSpanContext(),
                Link.Type.PARENT_LINKED_SPAN,
                Collections.singletonMap("key", AttributeValue.stringAttributeValue("value"))));

    verify(otelSpan).addLink(expectedOtelSpanContext(), Attributes.of(stringKey("key"), "value"));
  }

  private static io.opencensus.trace.SpanContext ocSpanContext() {
    return io.opencensus.trace.SpanContext.create(
        TraceId.fromLowerBase16(TRACE_ID),
        SpanId.fromLowerBase16(SPAN_ID),
        TraceOptions.DEFAULT,
        Tracestate.builder().build());
  }

  private static SpanContext expectedOtelSpanContext() {
    return SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault());
  }
}
