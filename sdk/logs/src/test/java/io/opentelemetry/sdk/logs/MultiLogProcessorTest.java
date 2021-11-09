/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.logs.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MultiLogProcessorTest {

  @Mock private LogProcessor logProcessor1;
  @Mock private LogProcessor logProcessor2;
  private static final LogData logData = TestUtil.createLogData(Severity.DEBUG, "message");

  @BeforeEach
  void setup() {
    when(logProcessor1.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());
    when(logProcessor2.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());
    when(logProcessor1.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(logProcessor2.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
  }

  @Test
  void empty() {
    LogProcessor multiLogProcessor = LogProcessor.composite();
    assertThat(multiLogProcessor).isInstanceOf(NoopLogProcessor.class);
    multiLogProcessor.emit(logData);
    multiLogProcessor.shutdown();
  }

  @Test
  void oneLogProcessor() {
    LogProcessor multiLogProcessor = LogProcessor.composite(logProcessor1);
    assertThat(multiLogProcessor).isSameAs(logProcessor1);
  }

  @Test
  void twoLogProcessor() {
    LogProcessor multiLogProcessor = LogProcessor.composite(logProcessor1, logProcessor2);
    multiLogProcessor.emit(logData);
    verify(logProcessor1).emit(same(logData));
    verify(logProcessor2).emit(same(logData));

    multiLogProcessor.forceFlush();
    verify(logProcessor1).forceFlush();
    verify(logProcessor2).forceFlush();

    multiLogProcessor.shutdown();
    verify(logProcessor1).shutdown();
    verify(logProcessor2).shutdown();
  }
}
