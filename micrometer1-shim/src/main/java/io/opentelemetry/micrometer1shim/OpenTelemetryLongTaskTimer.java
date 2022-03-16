/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.micrometer1shim;

import static io.opentelemetry.micrometer1shim.Bridging.name;
import static io.opentelemetry.micrometer1shim.Bridging.tagsAsAttributes;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.config.NamingConvention;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.internal.DefaultLongTaskTimer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableDoubleUpDownCounter;
import io.opentelemetry.api.metrics.ObservableLongUpDownCounter;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

final class OpenTelemetryLongTaskTimer extends DefaultLongTaskTimer implements RemovableMeter {

  private final DistributionStatisticConfig distributionStatisticConfig;
  private final ObservableLongUpDownCounter observableActiveTasks;
  private final ObservableDoubleUpDownCounter observableDuration;

  OpenTelemetryLongTaskTimer(
      Id id,
      NamingConvention namingConvention,
      Clock clock,
      TimeUnit baseTimeUnit,
      DistributionStatisticConfig distributionStatisticConfig,
      Meter otelMeter) {
    super(id, clock, baseTimeUnit, distributionStatisticConfig, false);

    this.distributionStatisticConfig = distributionStatisticConfig;

    String name = name(id, namingConvention);
    Attributes attributes = tagsAsAttributes(id, namingConvention);

    this.observableActiveTasks =
        otelMeter
            .upDownCounterBuilder(name + ".active")
            .setDescription(Bridging.description(id))
            .setUnit("tasks")
            .buildWithCallback(
                new LongMeasurementRecorder<>(this, DefaultLongTaskTimer::activeTasks, attributes));
    this.observableDuration =
        otelMeter
            .upDownCounterBuilder(name + ".duration")
            .ofDoubles()
            .setDescription(Bridging.description(id))
            .setUnit(TimeUnitHelper.getUnitString(baseTimeUnit))
            .buildWithCallback(
                new DoubleMeasurementRecorder<>(
                    this, t -> t.duration(t.baseTimeUnit()), attributes));
  }

  @Override
  public Iterable<Measurement> measure() {
    UnsupportedReadLogger.logWarning();
    return Collections.emptyList();
  }

  @Override
  public void onRemove() {
    observableActiveTasks.close();
    observableDuration.close();
  }

  boolean isUsingMicrometerHistograms() {
    return distributionStatisticConfig.isPublishingPercentiles()
        || distributionStatisticConfig.isPublishingHistogram();
  }
}
