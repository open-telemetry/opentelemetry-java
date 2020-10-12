package io.opentelemetry.sdk.metrics;

import io.opentelemetry.common.ReadWriteLabels;

public interface MetricsProcessor {
  void onMetricRecorded(AbstractSynchronousInstrument<?> instr, ReadWriteLabels labels,
      Object value);

  void onLabelsBound(AbstractSynchronousInstrument<?> instr, ReadWriteLabels labels);
}

/**
 * TODO:
 * 1. MetricsProcessor to enrich labels
 * 2. add propagation for Baggage
 * 3. explicit event manipulation code
 * <p>
 * <p>
 * Span-first:
 * 1. SpanProcessor to manipulate baggage
 * 2. prototype callbacks for event data?
 * <p>
 * <p>
 * when creating a span
 * Check local Context if there's an event - if it is - use it.
 * if no - check baggage for remote event. If it is there - put it in Context and use
 * if no - then we are to start event. Get event data from callback.
 * if no callback - exception/default event
 * MetricsProcessor to enrich labels from local event
 * SpanProcessor to do the same
 * <p>
 * SpanProcessor to manipulate baggage
 * callbacks in instrumentation
 */