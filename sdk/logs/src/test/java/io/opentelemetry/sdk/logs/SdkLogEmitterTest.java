/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.resources.Resource;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class SdkLogEmitterTest {

  @Test
  void logBuilder() {
    Instant now = Instant.now();
    LogEmitterSharedState state = mock(LogEmitterSharedState.class);
    InstrumentationLibraryInfo info = mock(InstrumentationLibraryInfo.class);
    AtomicReference<LogData> seenLog = new AtomicReference<>();
    LogProcessor logProcessor = seenLog::set;

    when(state.getResource()).thenReturn(Resource.getDefault());
    when(state.getLogProcessor()).thenReturn(logProcessor);

    SdkLogEmitter emitter = new SdkLogEmitter(state, info);
    LogBuilder logBuilder = emitter.logBuilder();
    logBuilder.setEpoch(now);
    logBuilder.setBody("foo");

    // Have to test through the builder
    logBuilder.emit();
    assertThat(seenLog.get().getBody().asString()).isEqualTo("foo");
  }
}
