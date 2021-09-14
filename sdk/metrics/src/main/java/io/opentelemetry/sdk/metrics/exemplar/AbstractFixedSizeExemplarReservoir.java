/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.exemplar;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.data.DoubleExemplar;
import io.opentelemetry.sdk.metrics.data.Exemplar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Base implementation for fixed-size reservoir sampling of Exemplars.
 *
 * <p>Additionally this implementation ONLY exports double valued exemplars.
 */
abstract class AbstractFixedSizeExemplarReservoir implements ExemplarReservoir {
  private final Clock clock;
  private final ReservoirCell[] storage;

  /**
   * Instantiates an exemplar reservoir of fixed size.
   *
   * @param clock The clock to use when annotating measurements with time.
   * @param size The number of exemplars to preserve.
   */
  AbstractFixedSizeExemplarReservoir(Clock clock, int size) {
    this.clock = clock;
    this.storage = new ReservoirCell[size];
    for (int i = 0; i < size; ++i) {
      this.storage[i] = new ReservoirCell();
    }
  }

  protected final int maxSize() {
    return storage.length;
  }
  /**
   * Determines the sample bucket for a given measurement.
   *
   * @return The bucket to sample into or -1 for no sampling.
   */
  protected abstract int bucketFor(double value, Attributes attributes, Context context);
  /** Callback to reset any local state after a {@link #collectAndReset} call. */
  protected void reset() {}

  @Override
  public final void offerMeasurement(long value, Attributes attributes, Context context) {
    offerMeasurement((double) value, attributes, context);
  }

  @Override
  public final void offerMeasurement(double value, Attributes attributes, Context context) {
    int bucket = bucketFor(value, attributes, context);
    if (bucket != -1) {
      this.storage[bucket].offerMeasurement(value, attributes, context);
    }
  }

  @Override
  public final List<Exemplar> collectAndReset(Attributes pointAttributes) {
    // Note: we are collecting exemplars from buckets piecemeal, but we
    // could still be sampling exemplars during this process.
    List<Exemplar> results = new ArrayList<>();
    for (int i = 0; i < this.storage.length; ++i) {
      Exemplar result = this.storage[i].getAndReset(pointAttributes);
      if (result != null) {
        results.add(result);
      }
    }
    reset();
    return Collections.unmodifiableList(results);
  }

  /**
   * A Reservoir cell pre-allocated memories for Exemplar data.
   *
   * <p>We only allocate new objects during collection. This class should NOT cause allocations
   * during sampling or within the synchronous metric hot-path.
   *
   * <p>Allocations are acceptable in the {@link #getAndReset(Attributes)} method.
   */
  private class ReservoirCell {
    private boolean hasValue = false;
    private double value;
    private Attributes attributes;
    private String spanId;
    private String traceId;
    private long recordTime;

    synchronized void offerMeasurement(double value, Attributes attributes, Context context) {
      this.value = value;
      this.attributes = attributes;
      // Note: It may make sense in the future to attempt to pull this from an active span.
      this.recordTime = clock.nanoTime();
      updateFromContext(context);
      hasValue = true;
    }

    private void updateFromContext(Context context) {
      Span current = Span.fromContext(context);
      if (current.getSpanContext().isValid()) {
        this.spanId = current.getSpanContext().getSpanId();
        this.traceId = current.getSpanContext().getTraceId();
      }
    }

    @Nullable
    synchronized Exemplar getAndReset(Attributes pointAttributes) {
      if (hasValue) {
        Exemplar result =
            DoubleExemplar.create(
                filtered(attributes, pointAttributes), recordTime, spanId, traceId, value);
        this.attributes = null;
        this.value = 0;
        this.spanId = null;
        this.traceId = null;
        this.recordTime = 0;
        this.hasValue = false;
        return result;
      }
      return null;
    }
  }

  /** Returns filtered attributes for exemplars. */
  @SuppressWarnings("unchecked")
  private static Attributes filtered(Attributes original, Attributes metricPoint) {
    if (metricPoint.isEmpty()) {
      return original;
    }
    AttributesBuilder result = Attributes.builder();
    Set<AttributeKey<?>> keys = metricPoint.asMap().keySet();
    original.forEach(
        (k, v) -> {
          if (!keys.contains(k)) {
            result.<Object>put((AttributeKey<Object>) k, v);
          }
        });
    return result.build();
  }
}
