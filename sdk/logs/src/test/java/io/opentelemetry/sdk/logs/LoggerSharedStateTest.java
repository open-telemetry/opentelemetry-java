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
import io.opentelemetry.sdk.resources.internal.SdkResourceProvider;
import org.junit.jupiter.api.Test;

class LoggerSharedStateTest {

  @Test
  void shutdown() {
    LogRecordProcessor logRecordProcessor = mock(LogRecordProcessor.class);
    CompletableResultCode code = new CompletableResultCode();
    when(logRecordProcessor.shutdown()).thenReturn(code);
    LoggerSharedState state =
        new LoggerSharedState(
            SdkResourceProvider.create(Resource.empty()),
            LogLimits::getDefault,
            logRecordProcessor,
            Clock.getDefault(),
            ExceptionAttributeResolver.getDefault(),
            new SdkLoggerInstrumentation(MeterProvider::noop),
            /* contextScopedAttributesFilter= */ null);
    state.shutdown();
    state.shutdown();
    verify(logRecordProcessor, times(1)).shutdown();
  }

  @Test
  void contextScopedAttributesFilter() {
    LoggerSharedState stateEnabled =
        new LoggerSharedState(
            SdkResourceProvider.create(Resource.empty()),
            LogLimits::getDefault,
            mock(LogRecordProcessor.class),
            Clock.getDefault(),
            ExceptionAttributeResolver.getDefault(),
            new SdkLoggerInstrumentation(MeterProvider::noop),
            (scope, key) -> true);
    assertThat(stateEnabled.getContextScopedAttributesFilter()).isNotNull();

    LoggerSharedState stateDisabled =
        new LoggerSharedState(
            SdkResourceProvider.create(Resource.empty()),
            LogLimits::getDefault,
            mock(LogRecordProcessor.class),
            Clock.getDefault(),
            ExceptionAttributeResolver.getDefault(),
            new SdkLoggerInstrumentation(MeterProvider::noop),
            /* contextScopedAttributesFilter= */ null);
    assertThat(stateDisabled.getContextScopedAttributesFilter()).isNull();
  }
}
