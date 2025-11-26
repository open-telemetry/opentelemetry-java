/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.testing.internal.AbstractDefaultMeterTest;
import org.junit.jupiter.api.Test;

class ExtendedDefaultMeterTest extends AbstractDefaultMeterTest {

  @Override
  protected Meter getMeter() {
    return ExtendedDefaultMeter.getNoop();
  }

  @Override
  protected MeterProvider getMeterProvider() {
    return ExtendedDefaultMeterProvider.getNoop();
  }

  @Test
  void incubatingApiIsLoaded() {
    Meter meter = MeterProvider.noop().get("test");
    assertThat(meter).isSameAs(OpenTelemetry.noop().getMeter("test"));

    assertThat(meter.gaugeBuilder("test").ofLongs()).isInstanceOf(ExtendedLongGaugeBuilder.class);
    assertThat(meter.gaugeBuilder("test").ofLongs().build())
        .isInstanceOfSatisfying(
            ExtendedLongGauge.class, instrument -> assertThat(instrument.isEnabled()).isFalse());
    assertThat(meter.gaugeBuilder("test")).isInstanceOf(ExtendedDoubleGaugeBuilder.class);
    assertThat(meter.gaugeBuilder("test").build())
        .isInstanceOfSatisfying(
            ExtendedDoubleGauge.class, instrument -> assertThat(instrument.isEnabled()).isFalse());

    assertThat(meter.histogramBuilder("test").ofLongs())
        .isInstanceOf(ExtendedLongHistogramBuilder.class);
    assertThat(meter.histogramBuilder("test").ofLongs().build())
        .isInstanceOfSatisfying(
            ExtendedLongHistogram.class,
            instrument -> assertThat(instrument.isEnabled()).isFalse());
    assertThat(meter.histogramBuilder("test")).isInstanceOf(ExtendedDoubleHistogramBuilder.class);
    assertThat(meter.histogramBuilder("test").build())
        .isInstanceOfSatisfying(
            ExtendedDoubleHistogram.class,
            instrument -> assertThat(instrument.isEnabled()).isFalse());

    assertThat(meter.counterBuilder("test")).isInstanceOf(ExtendedLongCounterBuilder.class);
    assertThat(meter.counterBuilder("test").build())
        .isInstanceOfSatisfying(
            ExtendedLongCounter.class, instrument -> assertThat(instrument.isEnabled()).isFalse());
    assertThat(meter.counterBuilder("test").ofDoubles())
        .isInstanceOf(ExtendedDoubleCounterBuilder.class);
    assertThat(meter.counterBuilder("test").ofDoubles().build())
        .isInstanceOfSatisfying(
            ExtendedDoubleCounter.class,
            instrument -> assertThat(instrument.isEnabled()).isFalse());

    assertThat(meter.upDownCounterBuilder("test"))
        .isInstanceOf(ExtendedLongUpDownCounterBuilder.class);
    assertThat(meter.upDownCounterBuilder("test").build())
        .isInstanceOfSatisfying(
            ExtendedLongUpDownCounter.class,
            instrument -> assertThat(instrument.isEnabled()).isFalse());
    assertThat(meter.upDownCounterBuilder("test").ofDoubles())
        .isInstanceOf(ExtendedDoubleUpDownCounterBuilder.class);
    assertThat(meter.upDownCounterBuilder("test").ofDoubles().build())
        .isInstanceOfSatisfying(
            ExtendedDoubleUpDownCounter.class,
            instrument -> assertThat(instrument.isEnabled()).isFalse());
  }
}
