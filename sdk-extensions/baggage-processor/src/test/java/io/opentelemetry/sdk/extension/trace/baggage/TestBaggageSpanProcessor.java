/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.baggage;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TestBaggageSpanProcessor {
  @Mock private SpanProcessor nextProcessor;
  @Mock private ReadWriteSpan readWriteSpan;

  @Test
  void ignoresEmptyBaggage() {
    BaggageSpanProcessor processor = BaggageSpanProcessor.builder(nextProcessor).build();

    Baggage empty = Baggage.empty();
    Context context = Context.root().with(empty);

    processor.onStart(context, readWriteSpan);
    Mockito.verify(nextProcessor).onStart(context, readWriteSpan);
    Mockito.verify(readWriteSpan, Mockito.never())
        .setAttribute(Mockito.anyString(), Mockito.anyString());
  }

  @Test
  void appendsAllBaggage() {
    BaggageSpanProcessor processor = BaggageSpanProcessor.builder(nextProcessor).build();
    Baggage empty = Baggage.builder().put("test1", "value").put("test2", "value").build();
    Context context = Context.root().with(empty);
    processor.onStart(context, readWriteSpan);
    Mockito.verify(nextProcessor).onStart(context, readWriteSpan);
    Mockito.verify(readWriteSpan).setAttribute("test1", "value");
    Mockito.verify(readWriteSpan).setAttribute("test2", "value");
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
    Mockito.verify(nextProcessor).onStart(context, readWriteSpan);
    Mockito.verify(readWriteSpan).setAttribute("test1", "value");
    Mockito.verify(readWriteSpan, Mockito.never()).setAttribute("test2", "value");
  }

  @Test
  void appendsWhenSpanFilter() {
    BaggageSpanProcessor processor =
        BaggageSpanProcessor.builder(nextProcessor)
            .setSpanFilter(span -> span.getKind() == SpanKind.CLIENT)
            .build();
    Mockito.when(readWriteSpan.getKind()).thenReturn(SpanKind.CLIENT);
    Baggage empty = Baggage.builder().put("test1", "value").build();
    Context context = Context.root().with(empty);
    processor.onStart(context, readWriteSpan);
    Mockito.verify(nextProcessor).onStart(context, readWriteSpan);
    Mockito.verify(readWriteSpan).setAttribute("test1", "value");
  }

  @Test
  void doesntAppendsWhenSpanFilterNegative() {
    BaggageSpanProcessor processor =
        BaggageSpanProcessor.builder(nextProcessor)
            .setSpanFilter(span -> span.getKind() == SpanKind.CLIENT)
            .build();
    Mockito.when(readWriteSpan.getKind()).thenReturn(SpanKind.SERVER);
    Baggage empty = Baggage.builder().put("test1", "value").build();
    Context context = Context.root().with(empty);
    processor.onStart(context, readWriteSpan);
    Mockito.verify(nextProcessor).onStart(context, readWriteSpan);
    Mockito.verify(readWriteSpan, Mockito.never()).setAttribute("test1", "value");
  }
}
