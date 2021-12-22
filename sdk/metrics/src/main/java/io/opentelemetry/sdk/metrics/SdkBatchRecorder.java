/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.BatchRecorder;
import io.opentelemetry.api.metrics.DoubleInstrument;
import io.opentelemetry.api.metrics.LongInstrument;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** {@link SdkBatchRecorder} is SDK implementation of {@link BatchRecorder}. */
final class SdkBatchRecorder implements BatchRecorder {

  private static final Logger logger = Logger.getLogger(SdkBatchRecorder.class.getName());

  private final ThrottlingLogger throttlingLogger = new ThrottlingLogger(logger);
  private final List<Object> measurements = new ArrayList<>();
  private final SdkMeter.BatchLatch batchLatch;
  private volatile boolean recorded = false;

  SdkBatchRecorder(SdkMeter.BatchLatch batchLatch) {
    this.batchLatch = batchLatch;
  }

  @Override
  public BatchRecorder addMeasurements(long value, LongInstrument... instruments) {
    if (!isRecorded()) {
      measurements.add(new LongMeasurement(value, instruments));
    }
    return this;
  }

  @Override
  public BatchRecorder addMeasurements(double value, DoubleInstrument... instruments) {
    if (!isRecorded()) {
      measurements.add(new DoubleMeasurement(value, instruments));
    }
    return this;
  }

  @Override
  public void record(Attributes attributes, Context context) {
    if (isRecorded()) {
      return;
    }
    recorded = true;

    batchLatch.startBatchRecord();
    try {
      for (Object measurement : measurements) {
        if (measurement instanceof DoubleMeasurement) {
          DoubleMeasurement doubleMeasurement = (DoubleMeasurement) measurement;
          double value = doubleMeasurement.value;
          for (DoubleInstrument instrument : doubleMeasurement.instruments) {
            if (instrument instanceof SdkDoubleHistogram) {
              ((SdkDoubleHistogram) instrument).record(value, attributes, context);
            } else if (instrument instanceof SdkDoubleCounter) {
              ((SdkDoubleCounter) instrument).add(value, attributes, context);
            } else if (instrument instanceof SdkDoubleUpDownCounter) {
              ((SdkDoubleUpDownCounter) instrument).add(value, attributes, context);
            } else {
              unrecognizedInstrument(instrument);
            }
          }
        }
        if (measurement instanceof LongMeasurement) {
          LongMeasurement longMeasurement = (LongMeasurement) measurement;
          long value = longMeasurement.value;
          for (LongInstrument instrument : longMeasurement.instruments) {
            if (instrument instanceof SdkLongHistogram) {
              ((SdkLongHistogram) instrument).record(value, attributes, context);
            } else if (instrument instanceof SdkLongCounter) {
              ((SdkLongCounter) instrument).add(value, attributes, context);
            } else if (instrument instanceof SdkLongUpDownCounter) {
              ((SdkLongUpDownCounter) instrument).add(value, attributes, context);
            } else {
              unrecognizedInstrument(instrument);
            }
          }
        }
      }
    } finally {
      batchLatch.finishBatchRecord();
    }
  }

  private void unrecognizedInstrument(Object instrument) {
    throttlingLogger.log(
        Level.WARNING,
        "Unrecognized instrument in batch recording: " + instrument.getClass().getName());
  }

  private boolean isRecorded() {
    if (recorded) {
      throttlingLogger.log(Level.WARNING, "Batch has already recorded.");
      return true;
    }
    return false;
  }

  private static class DoubleMeasurement {
    private final double value;
    private final List<DoubleInstrument> instruments;

    private DoubleMeasurement(double value, DoubleInstrument... instruments) {
      this.value = value;
      this.instruments = Arrays.asList(instruments);
    }
  }

  private static class LongMeasurement {
    private final long value;
    private final List<LongInstrument> instruments;

    private LongMeasurement(long value, LongInstrument... instruments) {
      this.value = value;
      this.instruments = Arrays.asList(instruments);
    }
  }
}
