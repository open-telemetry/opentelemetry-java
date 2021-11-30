/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.LogDataBuilder;
import io.opentelemetry.sdk.logs.data.Severity;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class SdkLogBuilderTest {

  @Test
  void buildAndEmit() {
    Instant now = Instant.now();
    String name = "skippy";
    String bodyStr = "body";
    String sevText = "sevText";
    Severity severity = Severity.DEBUG3;
    Attributes attrs = Attributes.empty();
    AtomicReference<LogData> seenLog = new AtomicReference<>();
    LogProcessor logProcessor = seenLog::set;

    LogEmitterSharedState state = mock(LogEmitterSharedState.class);
    LogDataBuilder delegate = mock(LogDataBuilder.class);
    LogData logData = mock(LogData.class);
    Context context = mock(Context.class);

    when(state.getLogProcessor()).thenReturn(logProcessor);
    when(delegate.build()).thenReturn(logData);

    SdkLogBuilder builder = new SdkLogBuilder(state, delegate);
    builder.setBody(bodyStr);
    verify(delegate).setBody(bodyStr);
    builder.setEpoch(123, TimeUnit.SECONDS);
    verify(delegate).setEpoch(123, TimeUnit.SECONDS);
    builder.setEpoch(now);
    verify(delegate).setEpoch(now);
    builder.setAttributes(attrs);
    verify(delegate).setAttributes(attrs);
    builder.setContext(context);
    verify(delegate).setSpanContextFromContext(context);
    builder.setName(name);
    verify(delegate).setName(name);
    builder.setSeverity(severity);
    verify(delegate).setSeverity(severity);
    builder.setSeverityText(sevText);
    verify(delegate).setSeverityText(sevText);
    builder.emit();
    assertThat(seenLog.get()).isSameAs(logData);
  }

  @Test
  void emitAfterShutdown() {
    LogEmitterSharedState state = mock(LogEmitterSharedState.class);
    LogDataBuilder delegate = mock(LogDataBuilder.class);

    when(state.hasBeenShutdown()).thenReturn(true);

    SdkLogBuilder builder = new SdkLogBuilder(state, delegate);
    builder.emit();
    verify(state, never()).getLogProcessor();
    verifyNoInteractions(delegate);
  }
}
