/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.AbstractDefaultMeterTest;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExtendedDefaultMeterTest extends AbstractDefaultMeterTest {

  @Override
  protected Meter getMeter() {
    return ExtendedDefaultMeter.getNoop();
  }

  @Override
  protected MeterProvider getMeterProvider() {
    return ExtendedDefaultMeterProvider.getNoop();
  }

  @Test
  public void incubatingApiIsLoaded() {
    Meter meter = MeterProvider.noop().get("test");
    assertThat(meter).isSameAs(OpenTelemetry.noop().getMeter("test"));

    Assertions.assertThat(meter.gaugeBuilder("test").ofLongs())
        .isInstanceOf(ExtendedLongGaugeBuilder.class);
    Assertions.assertThat(meter.gaugeBuilder("test").ofLongs().build())
        .isInstanceOf(ExtendedLongGauge.class);
    Assertions.assertThat(meter.gaugeBuilder("test"))
        .isInstanceOf(ExtendedDoubleGaugeBuilder.class);
    Assertions.assertThat(meter.gaugeBuilder("test").build())
        .isInstanceOf(ExtendedDoubleGauge.class);

    Assertions.assertThat(meter.histogramBuilder("test").ofLongs())
        .isInstanceOf(ExtendedLongHistogramBuilder.class);
    Assertions.assertThat(meter.histogramBuilder("test").ofLongs().build())
        .isInstanceOf(ExtendedLongHistogram.class);
    Assertions.assertThat(meter.histogramBuilder("test"))
        .isInstanceOf(ExtendedDoubleHistogramBuilder.class);
    Assertions.assertThat(meter.histogramBuilder("test").build())
        .isInstanceOf(ExtendedDoubleHistogram.class);

    Assertions.assertThat(meter.counterBuilder("test"))
        .isInstanceOf(ExtendedLongCounterBuilder.class);
    Assertions.assertThat(meter.counterBuilder("test").build())
        .isInstanceOf(ExtendedLongCounter.class);
    Assertions.assertThat(meter.counterBuilder("test").ofDoubles())
        .isInstanceOf(ExtendedDoubleCounterBuilder.class);
    Assertions.assertThat(meter.counterBuilder("test").ofDoubles().build())
        .isInstanceOf(ExtendedDoubleCounter.class);

    Assertions.assertThat(meter.upDownCounterBuilder("test"))
        .isInstanceOf(ExtendedLongUpDownCounterBuilder.class);
    Assertions.assertThat(meter.upDownCounterBuilder("test").build())
        .isInstanceOf(ExtendedLongUpDownCounter.class);
    Assertions.assertThat(meter.upDownCounterBuilder("test").ofDoubles())
        .isInstanceOf(ExtendedDoubleUpDownCounterBuilder.class);
    Assertions.assertThat(meter.upDownCounterBuilder("test").ofDoubles().build())
        .isInstanceOf(ExtendedDoubleUpDownCounter.class);
  }
}
