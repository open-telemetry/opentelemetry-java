/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.micrometer1shim;

import static io.opentelemetry.micrometer1shim.Bridging.baseUnit;
import static io.opentelemetry.micrometer1shim.Bridging.statisticInstrumentName;
import static io.opentelemetry.micrometer1shim.Bridging.tagsAsAttributes;

import io.micrometer.core.instrument.AbstractMeter;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.NamingConvention;
import io.opentelemetry.api.common.Attributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class OpenTelemetryMeter extends AbstractMeter implements Meter, RemovableMeter {

  private final List<AutoCloseable> observableInstruments;

  OpenTelemetryMeter(
      Id id,
      NamingConvention namingConvention,
      Iterable<Measurement> measurements,
      io.opentelemetry.api.metrics.Meter otelMeter) {
    super(id);
    Attributes attributes = tagsAsAttributes(id, namingConvention);

    List<AutoCloseable> observableInstruments = new ArrayList<>();
    for (Measurement measurement : measurements) {
      String name = statisticInstrumentName(id, measurement.getStatistic(), namingConvention);
      String description = Bridging.description(id);
      String baseUnit = baseUnit(id);
      DoubleMeasurementRecorder<Measurement> callback =
          new DoubleMeasurementRecorder<>(measurement, Measurement::getValue, attributes);

      switch (measurement.getStatistic()) {
        case TOTAL:
          // fall through
        case TOTAL_TIME:
        case COUNT:
          observableInstruments.add(
              otelMeter
                  .counterBuilder(name)
                  .ofDoubles()
                  .setDescription(description)
                  .setUnit(baseUnit)
                  .buildWithCallback(callback));
          break;

        case ACTIVE_TASKS:
          observableInstruments.add(
              otelMeter
                  .upDownCounterBuilder(name)
                  .ofDoubles()
                  .setDescription(description)
                  .setUnit(baseUnit)
                  .buildWithCallback(callback));
          break;

        case DURATION:
          // fall through
        case MAX:
        case VALUE:
        case UNKNOWN:
          observableInstruments.add(
              otelMeter
                  .gaugeBuilder(name)
                  .setDescription(description)
                  .setUnit(baseUnit)
                  .buildWithCallback(callback));
          break;
      }
    }
    this.observableInstruments = observableInstruments;
  }

  @Override
  public Iterable<Measurement> measure() {
    UnsupportedReadLogger.logWarning();
    return Collections.emptyList();
  }

  @Override
  public void onRemove() {
    try {
      for (AutoCloseable observableInstrument : observableInstruments) {
        observableInstrument.close();
      }
    } catch (Exception e) {
      throw new IllegalStateException("SDK instruments should never throw on close()", e);
    }
  }
}
