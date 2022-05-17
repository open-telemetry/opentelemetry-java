/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.CallbackRegistration;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.internal.state.SdkObservableMeasurement;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

class SdkObservableInstrumentTest {

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(SdkObservableInstrument.class);

  @Test
  @SuppressLogger(SdkObservableInstrument.class)
  void close() {
    MeterSharedState meterSharedState =
        spy(MeterSharedState.create(InstrumentationScopeInfo.empty(), Collections.emptyList()));
    CallbackRegistration callbackRegistration =
        CallbackRegistration.create(
            Collections.singletonList(
                SdkObservableMeasurement.create(
                    InstrumentationScopeInfo.create("meter"),
                    InstrumentDescriptor.create(
                        "my-instrument",
                        "description",
                        "unit",
                        InstrumentType.COUNTER,
                        InstrumentValueType.DOUBLE),
                    Collections.emptyList())),
            () -> {});

    SdkObservableInstrument observableInstrument =
        new SdkObservableInstrument(meterSharedState, callbackRegistration);

    // First call to close should trigger remove from meter shared state
    observableInstrument.close();
    verify(meterSharedState).removeCallback(callbackRegistration);
    logs.assertDoesNotContain("Instrument my-instrument has called close() multiple times.");

    // Close a second time should not trigger remove from meter shared state
    Mockito.reset(meterSharedState);
    observableInstrument.close();
    verify(meterSharedState, never()).removeCallback(callbackRegistration);
    logs.assertContains("Instrument my-instrument has called close() multiple times.");
  }
}
