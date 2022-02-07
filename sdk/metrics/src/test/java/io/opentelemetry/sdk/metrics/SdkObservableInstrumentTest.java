/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.sdk.metrics.internal.state.AsynchronousMetricStorage;
import java.util.Arrays;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

class SdkObservableInstrumentTest {

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(SdkObservableInstrument.class);

  @Test
  @SuppressWarnings("unchecked")
  void remove() {
    AsynchronousMetricStorage<?, ObservableDoubleMeasurement> storage1 =
        mock(AsynchronousMetricStorage.class);
    AsynchronousMetricStorage<?, ObservableDoubleMeasurement> storage2 =
        mock(AsynchronousMetricStorage.class);

    Consumer<ObservableDoubleMeasurement> callback = unused -> {};
    SdkObservableInstrument<ObservableDoubleMeasurement> observableInstrument =
        new SdkObservableInstrument<>("my-instrument", Arrays.asList(storage1, storage2), callback);

    // First call to removal should trigger remove from storage
    observableInstrument.close();
    verify(storage1).removeCallback(callback);
    verify(storage2).removeCallback(callback);
    logs.assertDoesNotContain("Instrument my-instrument has called remove() multiple times.");

    // Removal a second time should not trigger remove from storage and should log a warning
    Mockito.reset(storage1, storage2);
    observableInstrument.close();
    verify(storage1, never()).removeCallback(any());
    verify(storage2, never()).removeCallback(any());
    logs.assertContains("Instrument my-instrument has called remove() multiple times.");
  }
}
