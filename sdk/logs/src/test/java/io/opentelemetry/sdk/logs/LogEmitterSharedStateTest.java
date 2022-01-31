/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class LogEmitterSharedStateTest {

  @Test
  void shutdown() {
    LogProcessor logProcessor = mock(LogProcessor.class);
    CompletableResultCode code = new CompletableResultCode();
    when(logProcessor.shutdown()).thenReturn(code);
    List<LogProcessor> processors = Collections.singletonList(logProcessor);
    LogEmitterSharedState state =
        new LogEmitterSharedState(
            Resource.empty(), LogLimits::getDefault, processors, Clock.getDefault());
    state.shutdown();
    state.shutdown();
    verify(logProcessor, times(1)).shutdown();
  }
}
