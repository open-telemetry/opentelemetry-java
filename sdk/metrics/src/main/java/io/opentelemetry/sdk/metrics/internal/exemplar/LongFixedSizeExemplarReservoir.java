/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.internal.concurrent.AdderUtil;
import io.opentelemetry.sdk.metrics.internal.concurrent.LongAdder;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongExemplarData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * Base implementation for fixed-size reservoir sampling of Exemplars.
 *
 * <p>Additionally this implementation ONLY exports double valued exemplars.
 */
// TODO(anuraaga): Reduce copy-paste from AbstractDoubleFixedSizeExemplarReservoir.
final class LongFixedSizeExemplarReservoir implements LongExemplarReservoir {
  private final LongAdder numMeasurements = AdderUtil.createLongAdder();

  private final Clock clock;
  private final ReservoirCell[] storage;
  private final Supplier<Random> randomSupplier;

  /** Instantiates an exemplar reservoir of fixed size. */
  LongFixedSizeExemplarReservoir(Clock clock, int size, Supplier<Random> randomSupplier) {
    this.randomSupplier = randomSupplier;
    this.clock = clock;
    this.storage = new ReservoirCell[size];
    for (int i = 0; i < size; ++i) {
      this.storage[i] = new ReservoirCell();
    }
  }

  int maxSize() {
    return storage.length;
  }

  /**
   * Determines the sample reservoir index for a given measurement.
   *
   * @return The index to sample into or -1 for no sampling.
   */
  int reservoirIndexFor(long value, Attributes attributes, Context context) {
    int count = numMeasurements.intValue() + 1;
    int index = this.randomSupplier.get().nextInt(count > 0 ? count : 1);
    numMeasurements.increment();
    if (index < maxSize()) {
      return index;
    }
    return -1;
  }

  @Override
  public void offerMeasurement(long value, Attributes attributes, Context context) {
    int bucket = reservoirIndexFor(value, attributes, context);
    if (bucket != -1) {
      this.storage[bucket].offerMeasurement(value, attributes, context);
    }
  }

  @Override
  public List<LongExemplarData> collectAndReset(Attributes pointAttributes) {
    // Note: we are collecting exemplars from buckets piecemeal, but we
    // could still be sampling exemplars during this process.
    List<LongExemplarData> results = new ArrayList<>();
    for (ReservoirCell reservoirCell : this.storage) {
      LongExemplarData result = reservoirCell.getAndReset(pointAttributes);
      if (result != null) {
        results.add(result);
      }
    }
    numMeasurements.reset();
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
    private long value;
    @Nullable private Attributes attributes;
    private SpanContext spanContext = SpanContext.getInvalid();
    private long recordTime;

    synchronized void offerMeasurement(long value, Attributes attributes, Context context) {
      this.value = value;
      this.attributes = attributes;
      // Note: It may make sense in the future to attempt to pull this from an active span.
      this.recordTime = clock.now();
      updateFromContext(context);
    }

    private void updateFromContext(Context context) {
      Span current = Span.fromContext(context);
      if (current.getSpanContext().isValid()) {
        this.spanContext = current.getSpanContext();
      }
    }

    @Nullable
    synchronized LongExemplarData getAndReset(Attributes pointAttributes) {
      Attributes attributes = this.attributes;
      if (attributes != null) {
        LongExemplarData result =
            ImmutableLongExemplarData.create(
                filtered(attributes, pointAttributes), recordTime, spanContext, value);
        this.attributes = null;
        this.value = 0;
        this.spanContext = SpanContext.getInvalid();
        this.recordTime = 0;
        return result;
      }
      return null;
    }
  }

  /** Returns filtered attributes for exemplars. */
  private static Attributes filtered(Attributes original, Attributes metricPoint) {
    if (metricPoint.isEmpty()) {
      return original;
    }
    Set<AttributeKey<?>> metricPointKeys = metricPoint.asMap().keySet();
    return original.toBuilder().removeIf(metricPointKeys::contains).build();
  }
}
