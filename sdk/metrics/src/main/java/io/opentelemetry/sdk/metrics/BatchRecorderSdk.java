/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.BatchRecorder;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.DoubleValueRecorder;
import io.opentelemetry.api.metrics.Instrument;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.LongValueRecorder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal implementation of the {@link BatchRecorder} that simply redirects the calls to the
 * instruments.
 */
final class BatchRecorderSdk implements BatchRecorder {
  private final Labels labelSet;

  private volatile Map<Instrument, List<Number>> recordings = new HashMap<>();
  private volatile Map<Instrument, List<Number>> previousRecordings = new HashMap<>();

  BatchRecorderSdk(String... keyValuePairs) {
    this.labelSet = Labels.of(keyValuePairs);
  }

  @Override
  public BatchRecorder put(LongValueRecorder valueRecorder, long value) {
    recordings.computeIfAbsent(valueRecorder, instrument -> new ArrayList<>()).add(value);
    return this;
  }

  @Override
  public BatchRecorder put(DoubleValueRecorder valueRecorder, double value) {
    recordings.computeIfAbsent(valueRecorder, instrument -> new ArrayList<>()).add(value);
    return this;
  }

  @Override
  public BatchRecorder put(LongCounter counter, long value) {
    recordings.computeIfAbsent(counter, instrument -> new ArrayList<>()).add(value);
    return this;
  }

  @Override
  public BatchRecorder put(DoubleCounter counter, double value) {
    recordings.computeIfAbsent(counter, instrument -> new ArrayList<>()).add(value);
    return this;
  }

  @Override
  public BatchRecorder put(LongUpDownCounter upDownCounter, long value) {
    recordings.computeIfAbsent(upDownCounter, instrument -> new ArrayList<>()).add(value);
    return this;
  }

  @Override
  public BatchRecorder put(DoubleUpDownCounter upDownCounter, double value) {
    recordings.computeIfAbsent(upDownCounter, instrument -> new ArrayList<>()).add(value);
    return this;
  }

  @Override
  public void record() {
    previousRecordings.clear();

    Map<Instrument, List<Number>> temp = recordings;
    recordings = previousRecordings;
    previousRecordings = temp;

    previousRecordings.forEach(
        (instrument, numbers) -> {
          if (instrument instanceof DoubleUpDownCounter) {
            for (Number number : numbers) {
              ((DoubleUpDownCounter) instrument).add(number.doubleValue(), labelSet);
            }
          }
          if (instrument instanceof DoubleCounter) {
            for (Number number : numbers) {
              ((DoubleCounter) instrument).add(number.doubleValue(), labelSet);
            }
          }
          if (instrument instanceof DoubleValueRecorder) {
            for (Number number : numbers) {
              ((DoubleValueRecorder) instrument).record(number.doubleValue(), labelSet);
            }
          }
          if (instrument instanceof LongUpDownCounter) {
            for (Number number : numbers) {
              ((LongUpDownCounter) instrument).add(number.longValue(), labelSet);
            }
          }
          if (instrument instanceof LongCounter) {
            for (Number number : numbers) {
              ((LongCounter) instrument).add(number.longValue(), labelSet);
            }
          }
          if (instrument instanceof LongValueRecorder) {
            for (Number number : numbers) {
              ((LongValueRecorder) instrument).record(number.longValue(), labelSet);
            }
          }
        });
  }
}
