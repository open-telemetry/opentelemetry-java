/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.baggage;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestBaggageSpanProcessor {
  @Mock private SpanProcessor nextProcessor;
  @Mock private ReadWriteSpan readWriteSpan;

  @Test
  void ignoresEmptyBaggage() {
    BaggageSpanProcessor processor = BaggageSpanProcessor.builder(nextProcessor).build();

    Baggage empty = Baggage.empty();
    Context context = Context.root().with(empty);

    processor.onStart(context, readWriteSpan);
    verify(nextProcessor).onStart(context, readWriteSpan);
    verify(readWriteSpan, never()).setAttribute(anyString(), anyString());
  }

  @Test
  void appendsAllBaggage() {
    BaggageSpanProcessor processor = BaggageSpanProcessor.builder(nextProcessor).build();
    Baggage empty = Baggage.builder().put("test1", "value").put("test2", "value").build();
    Context context = Context.root().with(empty);
    processor.onStart(context, readWriteSpan);
    verify(nextProcessor).onStart(context, readWriteSpan);
    verify(readWriteSpan).setAttribute("test1", "value");
    verify(readWriteSpan).setAttribute("test2", "value");
  }

  @Test
  void filtersBaggageByKey() {
    BaggageSpanProcessor processor =
        BaggageSpanProcessor.builder(nextProcessor)
            .filterBaggageAttributes(key -> "test1".equals(key))
            .build();
    Baggage empty = Baggage.builder().put("test1", "value").put("test2", "value").build();
    Context context = Context.root().with(empty);
    processor.onStart(context, readWriteSpan);
    verify(nextProcessor).onStart(context, readWriteSpan);
    verify(readWriteSpan).setAttribute("test1", "value");
    verify(readWriteSpan, never()).setAttribute("test2", "value");
  }

  @Test
  void appendsWhenSpanFilter() {
    BaggageSpanProcessor processor =
        BaggageSpanProcessor.builder(nextProcessor)
            .setSpanFilter(span -> span.getKind() == SpanKind.CLIENT)
            .build();
    when(readWriteSpan.getKind()).thenReturn(SpanKind.CLIENT);
    Baggage empty = Baggage.builder().put("test1", "value").build();
    Context context = Context.root().with(empty);
    processor.onStart(context, readWriteSpan);
    verify(nextProcessor).onStart(context, readWriteSpan);
    verify(readWriteSpan).setAttribute("test1", "value");
  }

  @Test
  void doesntAppendsWhenSpanFilterNegative() {
    BaggageSpanProcessor processor =
        BaggageSpanProcessor.builder(nextProcessor)
            .setSpanFilter(span -> span.getKind() == SpanKind.CLIENT)
            .build();
    when(readWriteSpan.getKind()).thenReturn(SpanKind.SERVER);
    Baggage empty = Baggage.builder().put("test1", "value").build();
    Context context = Context.root().with(empty);
    processor.onStart(context, readWriteSpan);
    verify(nextProcessor).onStart(context, readWriteSpan);
    verify(readWriteSpan, never()).setAttribute("test1", "value");
  }
}
