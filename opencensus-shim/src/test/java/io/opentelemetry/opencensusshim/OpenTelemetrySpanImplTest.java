/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opencensus.trace.Status;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OpenTelemetrySpanImplTest {

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
}
