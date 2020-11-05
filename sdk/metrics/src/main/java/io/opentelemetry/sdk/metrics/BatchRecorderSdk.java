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
import java.util.List;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

/**
 * Minimal implementation of the {@link BatchRecorder} that simply redirects the calls to the
 * instruments.
 */
final class BatchRecorderSdk implements BatchRecorder {
  private final Labels labelSet;

  // todo: this queue is unbounded; should we make it bounded and drop recordings after it gets
  // full?
  private final TransferQueue<Recording> pendingRecordings = new LinkedTransferQueue<>();

  BatchRecorderSdk(String... keyValuePairs) {
    this.labelSet = Labels.of(keyValuePairs);
  }

  @Override
  public BatchRecorder put(LongValueRecorder valueRecorder, long value) {
    pendingRecordings.offer(new Recording(valueRecorder, value));
    return this;
  }

  @Override
  public BatchRecorder put(DoubleValueRecorder valueRecorder, double value) {
    pendingRecordings.offer(new Recording(valueRecorder, value));
    return this;
  }

  @Override
  public BatchRecorder put(LongCounter counter, long value) {
    pendingRecordings.offer(new Recording(counter, value));
    return this;
  }

  @Override
  public BatchRecorder put(DoubleCounter counter, double value) {
    pendingRecordings.offer(new Recording(counter, value));
    return this;
  }

  @Override
  public BatchRecorder put(LongUpDownCounter upDownCounter, long value) {
    pendingRecordings.offer(new Recording(upDownCounter, value));
    return this;
  }

  @Override
  public BatchRecorder put(DoubleUpDownCounter upDownCounter, double value) {
    pendingRecordings.offer(new Recording(upDownCounter, value));
    return this;
  }

  @Override
  public void record() {
    List<Recording> recordings = new ArrayList<>();
    pendingRecordings.drainTo(recordings);

    recordings.forEach(
        (recording) -> {
          Instrument instrument = recording.instrument;
          Number value = recording.value;
          if (instrument instanceof DoubleUpDownCounter) {
            ((DoubleUpDownCounter) instrument).add(value.doubleValue(), labelSet);
          } else if (instrument instanceof DoubleCounter) {
            ((DoubleCounter) instrument).add(value.doubleValue(), labelSet);
          } else if (instrument instanceof DoubleValueRecorder) {
            ((DoubleValueRecorder) instrument).record(value.doubleValue(), labelSet);
          } else if (instrument instanceof LongUpDownCounter) {
            ((LongUpDownCounter) instrument).add(value.longValue(), labelSet);
          } else if (instrument instanceof LongCounter) {
            ((LongCounter) instrument).add(value.longValue(), labelSet);
          } else if (instrument instanceof LongValueRecorder) {
            ((LongValueRecorder) instrument).record(value.longValue(), labelSet);
          }
        });
  }

  private static class Recording {
    private final Instrument instrument;
    private final Number value;

    private Recording(Instrument instrument, Number value) {
      this.instrument = instrument;
      this.value = value;
    }
  }
}
