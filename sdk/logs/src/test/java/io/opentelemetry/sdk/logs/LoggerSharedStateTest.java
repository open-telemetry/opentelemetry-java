/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.internal.ExceptionAttributeResolver;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

class LoggerSharedStateTest {

  @Test
  void shutdown() {
    LogRecordProcessor logRecordProcessor = mock(LogRecordProcessor.class);
    CompletableResultCode code = new CompletableResultCode();
    when(logRecordProcessor.shutdown()).thenReturn(code);
    LoggerSharedState state =
        new LoggerSharedState(
            Resource.empty(),
            LogLimits::getDefault,
            logRecordProcessor,
            Clock.getDefault(),
            ExceptionAttributeResolver.getDefault(),
            new SdkLoggerInstrumentation(MeterProvider::noop),
            /* contextScopedAttributesEnabled= */ false);
    state.shutdown();
    state.shutdown();
    verify(logRecordProcessor, times(1)).shutdown();
  }

  @Test
  void contextScopedAttributesEnabled() {
    LoggerSharedState stateEnabled =
        new LoggerSharedState(
            Resource.empty(),
            LogLimits::getDefault,
            mock(LogRecordProcessor.class),
            Clock.getDefault(),
            ExceptionAttributeResolver.getDefault(),
            new SdkLoggerInstrumentation(MeterProvider::noop),
            /* contextScopedAttributesEnabled= */ true);
    assertThat(stateEnabled.isContextScopedAttributesEnabled()).isTrue();

    LoggerSharedState stateDisabled =
        new LoggerSharedState(
            Resource.empty(),
            LogLimits::getDefault,
            mock(LogRecordProcessor.class),
            Clock.getDefault(),
            ExceptionAttributeResolver.getDefault(),
            new SdkLoggerInstrumentation(MeterProvider::noop),
            /* contextScopedAttributesEnabled= */ false);
    assertThat(stateDisabled.isContextScopedAttributesEnabled()).isFalse();
  }
}
