/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.util.concurrent.AtomicDouble;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.data.DoubleExemplar;
import io.opentelemetry.sdk.metrics.data.Exemplar;
import io.opentelemetry.sdk.metrics.data.LongExemplar;
import io.opentelemetry.sdk.metrics.state.ExemplarReservoir;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

public class SynchronousHandleTest {

  @Test
  void acquireMapped() {
    TestSynchronousHandle storage = new TestSynchronousHandle();
    assertThat(storage.acquire()).isTrue();
    storage.release();
    assertThat(storage.acquire()).isTrue();
    assertThat(storage.acquire()).isTrue();
    storage.release();
    assertThat(storage.acquire()).isTrue();
    storage.release();
    storage.release();
  }

  @Test
  void tryUnmap_AcquiredHandler() {
    TestSynchronousHandle storage = new TestSynchronousHandle();
    assertThat(storage.acquire()).isTrue();
    assertThat(storage.tryUnmap()).isFalse();
    storage.release();
    // The aggregator is by default acquired, so need an extra release.
    assertThat(storage.tryUnmap()).isFalse();
    storage.release();
    assertThat(storage.tryUnmap()).isTrue();
  }

  @Test
  void tryUnmap_AcquiredHandler_MultipleTimes() {
    TestSynchronousHandle storage = new TestSynchronousHandle();
    assertThat(storage.acquire()).isTrue();
    assertThat(storage.acquire()).isTrue();
    assertThat(storage.acquire()).isTrue();
    assertThat(storage.tryUnmap()).isFalse();
    storage.release();
    assertThat(storage.acquire()).isTrue();
    assertThat(storage.tryUnmap()).isFalse();
    storage.release();
    assertThat(storage.tryUnmap()).isFalse();
    storage.release();
    assertThat(storage.tryUnmap()).isFalse();
    storage.release();
    // The aggregator is by default acquired, so need an extra release.
    assertThat(storage.tryUnmap()).isFalse();
    storage.release();
    assertThat(storage.tryUnmap()).isTrue();
  }

  @Test
  void bind_ThenUnmap_ThenTryToBind() {
    TestSynchronousHandle storage = new TestSynchronousHandle();
    storage.release();
    assertThat(storage.tryUnmap()).isTrue();
    assertThat(storage.acquire()).isFalse();
    storage.release();
  }

  @Test
  void testRecordings() {
    TestSynchronousHandle storage = new TestSynchronousHandle();

    storage.recordLong(22, Attributes.empty(), Context.root());
    assertThat(storage.recordedLong.get()).isEqualTo(22);
    assertThat(storage.recordedDouble.get()).isEqualTo(0);

    storage.accumulateThenReset(Attributes.empty());
    assertThat(storage.recordedLong.get()).isEqualTo(0);
    assertThat(storage.recordedDouble.get()).isEqualTo(0);

    storage.recordDouble(33.55, Attributes.empty(), Context.root());
    assertThat(storage.recordedLong.get()).isEqualTo(0);
    assertThat(storage.recordedDouble.get()).isEqualTo(33.55);

    storage.accumulateThenReset(Attributes.empty());
    assertThat(storage.recordedLong.get()).isEqualTo(0);
    assertThat(storage.recordedDouble.get()).isEqualTo(0);
  }

  @Test
  void testExemplars() {
    TestSynchronousHandle storage = new TestSynchronousHandle(new EverythingResorvoir());

    // First record one measurement as exemplar and see if it is passed correctly.
    final Exemplar firstMeasurement =
        LongExemplar.create(
            Attributes.empty(), /*recordTimeNanos=*/ 0L, /*trace_id*/ null, /*span_id*/ null, 22);
    storage.recordLong(22, Attributes.empty(), Context.root());
    assertThat(storage.recordedLong.get()).isEqualTo(22);
    assertThat(storage.recordedDouble.get()).isEqualTo(0);
    assertThat(storage.recordedExemplars.get()).isEmpty();

    storage.accumulateThenReset(Attributes.empty());
    assertThat(storage.recordedLong.get()).isEqualTo(0);
    assertThat(storage.recordedDouble.get()).isEqualTo(0);
    assertThat(storage.recordedExemplars.get()).containsExactlyInAnyOrder(firstMeasurement);

    // Now record two measurements and see if they both get sampled as exemplar.
    final Exemplar secondMeasurement =
        LongExemplar.create(
            Attributes.empty(), /*recordTimeNanos=*/ 0L, /*trace_id*/ null, /*span_id*/ null, 44);
    final Exemplar thirdMeasurement =
        LongExemplar.create(
            Attributes.empty(), /*recordTimeNanos=*/ 0L, /*trace_id*/ null, /*span_id*/ null, 33);
    storage.recordLong(44, Attributes.empty(), Context.root());
    assertThat(storage.recordedLong.get()).isEqualTo(44);
    storage.recordLong(33, Attributes.empty(), Context.root());
    assertThat(storage.recordedLong.get()).isEqualTo(33);
    storage.accumulateThenReset(Attributes.empty());
    assertThat(storage.recordedExemplars.get())
        .containsExactlyInAnyOrder(secondMeasurement, thirdMeasurement);
  }

  private static class TestSynchronousHandle extends SynchronousHandle<Void> {
    final AtomicLong recordedLong = new AtomicLong();
    final AtomicDouble recordedDouble = new AtomicDouble();
    final AtomicReference<List<Exemplar>> recordedExemplars =
        new AtomicReference<>(Collections.emptyList());

    TestSynchronousHandle() {
      this(ExemplarReservoir.EMPTY);
    }

    TestSynchronousHandle(ExemplarReservoir sampler) {
      super(sampler);
    }

    @Nullable
    @Override
    protected Void doAccumulateThenReset(List<Exemplar> exemplars) {
      recordedLong.set(0);
      recordedDouble.set(0);
      recordedExemplars.set(exemplars);
      return null;
    }

    @Override
    protected void doRecordLong(long value, Attributes attributes, Context context) {
      recordedLong.lazySet(value);
    }

    @Override
    protected void doRecordDouble(double value, Attributes attributes, Context context) {
      recordedDouble.lazySet(value);
    }
  }

  /** Test class that stores ALL exemplars offered. Thread-Unsafe. */
  private static class EverythingResorvoir implements ExemplarReservoir {
    private List<Exemplar> collection = new ArrayList<>();

    @Override
    public void offerMeasurementLong(long value, Attributes attributes, Context context) {
      collection.add(LongExemplar.create(attributes, 0, null, null, value));
    }

    @Override
    public void offerMeasurementDouble(double value, Attributes attributes, Context context) {
      collection.add(DoubleExemplar.create(attributes, 0, null, null, value));
    }

    @Override
    public List<Exemplar> collectAndReset(Attributes pointAttributes) {
      List<Exemplar> result = collection;
      this.collection = new ArrayList<>();
      return result;
    }
  }
}
