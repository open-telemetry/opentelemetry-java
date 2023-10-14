/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongExemplarData;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpanUpdatingExemplarReservoirTest {
  private static final AttributeKey<Boolean> KEY = AttributeKey.booleanKey("otel.exemplar");
  private static final SpanBuilder spanBuilder =
    SdkTracerProvider.builder().build().get("").spanBuilder("");
  @Mock ExemplarReservoir<DoubleExemplarData> doubleParent;
  @Mock ExemplarReservoir<LongExemplarData> longParent;
  @Spy Span span = spanBuilder.startSpan();

  @Test
  void offerDoubleMeasurement_parentRefused() {
    when(doubleParent.offerDoubleMeasurement(anyDouble(), any(), any())).thenReturn(false);
    SpanUpdatingExemplarReservoir<DoubleExemplarData> reservoir =
        new SpanUpdatingExemplarReservoir<>(doubleParent);
    assertThat(reservoir.offerDoubleMeasurement(1.0, Attributes.empty(), Context.root().with(span)))
        .isFalse();

    verify(span, never()).setAttribute(any(), anyBoolean());
  }

  @Test
  void offerDoubleMeasurement_parentAccepted() {
    doReturn(true).when(doubleParent).offerDoubleMeasurement(anyDouble(), any(), any());
    SpanUpdatingExemplarReservoir<DoubleExemplarData> reservoir =
        new SpanUpdatingExemplarReservoir<>(doubleParent);
    assertThat(reservoir.offerDoubleMeasurement(1.0, Attributes.empty(), Context.root().with(span)))
        .isTrue();

    verify(span).setAttribute(KEY, true);
  }

  @Test
  void offerLongMeasurement_parentRefused() {
    when(longParent.offerLongMeasurement(anyLong(), any(), any())).thenReturn(false);
    SpanUpdatingExemplarReservoir<LongExemplarData> reservoir =
        new SpanUpdatingExemplarReservoir<>(longParent);
    assertThat(reservoir.offerLongMeasurement(1L, Attributes.empty(), Context.root().with(span)))
        .isFalse();

    verify(span, never()).setAttribute(any(), anyBoolean());
  }

  @Test
  void offerLongMeasurement_parentAccepted() {
    doReturn(true).when(longParent).offerLongMeasurement(anyLong(), any(), any());
    SpanUpdatingExemplarReservoir<LongExemplarData> reservoir =
        new SpanUpdatingExemplarReservoir<>(longParent);
    assertThat(reservoir.offerLongMeasurement(1L, Attributes.empty(), Context.root().with(span)))
        .isTrue();

    verify(span).setAttribute(KEY, true);
  }

  @Test
  void collectAndReset() {
    SpanUpdatingExemplarReservoir<LongExemplarData> reservoir =
        new SpanUpdatingExemplarReservoir<>(longParent);
    Attributes attributes = Attributes.of(AttributeKey.stringKey("test"), "!");
    List<LongExemplarData> data = Arrays.asList(
        ImmutableLongExemplarData.create(Attributes.empty(), 123L, SpanContext.getInvalid(), 456L));
    doReturn(data).when(longParent).collectAndReset(attributes);
    assertThat(reservoir.collectAndReset(attributes))
        .isEqualTo(data);
  }
}
