/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.data.DoubleExemplar;
import io.opentelemetry.sdk.metrics.data.Exemplar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FixedSizeExemplarReservoir implements ExemplarReservoir {
  // TODO: Custom Random/Clock with low thread-contention.
  private final Clock clock;
  private final ResorvoirCell[] storage;
  private final LongAdder numMeasurements = new LongAdder();

  public FixedSizeExemplarReservoir(Clock clock, int size) {
    this.clock = clock;
    this.storage = new ResorvoirCell[size];
    for (int i = 0; i < size; ++i) {
      this.storage[i] = new ResorvoirCell();
    }
  }

  // TODO: we're using unweighted resorvoir sampler here.
  // We can make this more intelligent/pluggable going forward.
  private int nextBucket() {
    // TODO - limit the thread local to ONLY spans being recorded....
    long index = ThreadLocalRandom.current().nextLong(numMeasurements.sum() + 1);
    if (index < storage.length) {
      return (int) index;
    }
    return -1;
  }

  @Override
  public void offerMeasurementLong(long value, Attributes attributes, Context context) {
    int bucket = nextBucket();
    if (bucket != -1) {
      this.storage[bucket].offerMeasurementLong(value, attributes, context);
    }
    numMeasurements.increment();
  }

  @Override
  public void offerMeasurementDouble(double value, Attributes attributes, Context context) {
    int bucket = nextBucket();
    if (bucket != -1) {
      this.storage[bucket].offerMeasurementDouble(value, attributes, context);
    }
    numMeasurements.increment();
  }

  @Override
  public List<Exemplar> collectAndReset(Attributes pointAttributes) {
    List<Exemplar> results = new ArrayList<>();
    // TODO: Note exemplars COULD BE COMING IN while we iterate + reset.
    for (int i = 0; i < this.storage.length; ++i) {
      Exemplar result = this.storage[i].getAndReset(pointAttributes);
      if (result != null) {
        results.add(result);
      }
    }
    // Reset the count so exemplars are likely to be filled.
    numMeasurements.reset();
    return Collections.unmodifiableList(results);
  }

  private class ResorvoirCell {
    private final Lock lock = new ReentrantLock();

    private boolean hasValue = false;
    private double value;
    private Attributes attributes;
    private String spanId;
    private String traceId;
    private long recordTime;

    public void offerMeasurementLong(long value, Attributes attributes, Context context) {
      lock.lock();
      try {
        this.value = value;
        this.attributes = attributes;
        this.recordTime = clock.nanoTime(); // TODO - pull high resolution from span?
        updateFromContext(context);
        hasValue = true;
      } finally {
        lock.unlock();
      }
    }

    public void offerMeasurementDouble(double value, Attributes attributes, Context context) {
      lock.lock();
      try {
        this.value = value;
        this.attributes = attributes;
        this.recordTime = clock.nanoTime(); // TODO - pull high resolution from span?
        updateFromContext(context);
        hasValue = true;
      } finally {
        lock.unlock();
      }
    }

    @GuardedBy("lock")
    private void updateFromContext(Context context) {
      Span current = Span.fromContext(context);
      if (current.getSpanContext().isValid()) {
        this.spanId = current.getSpanContext().getSpanId();
        this.traceId = current.getSpanContext().getTraceId();
      }
    }

    public Exemplar getAndReset(Attributes pointAttributes) {
      lock.lock();
      try {
        if (hasValue) {
          Exemplar result =
              DoubleExemplar.create(
                  attributes.removeAll(pointAttributes), recordTime, spanId, traceId, value);
          this.attributes = null;
          this.value = 0;
          this.spanId = null;
          this.traceId = null;
          this.recordTime = 0;
          this.hasValue = false;
          return result;
        }
        return null;
      } finally {
        lock.unlock();
      }
    }
  }
}
