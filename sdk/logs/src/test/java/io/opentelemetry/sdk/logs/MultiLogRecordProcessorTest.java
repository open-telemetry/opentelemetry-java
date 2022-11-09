/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MultiLogRecordProcessorTest {

  @Mock private LogRecordProcessor logRecordProcessor1;
  @Mock private LogRecordProcessor logRecordProcessor2;
  @Mock private ReadWriteLogRecord logRecord;

  @BeforeEach
  void setup() {
    when(logRecordProcessor1.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());
    when(logRecordProcessor2.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());
    when(logRecordProcessor1.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(logRecordProcessor2.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
  }

  @Test
  void empty() {
    LogRecordProcessor multiLogRecordProcessor = LogRecordProcessor.composite();
    assertThat(multiLogRecordProcessor).isInstanceOf(NoopLogRecordProcessor.class);
    multiLogRecordProcessor.onEmit(Context.current(), logRecord);
    multiLogRecordProcessor.shutdown();
  }

  @Test
  void oneLogRecordProcessor() {
    LogRecordProcessor multiLogRecordProcessor = LogRecordProcessor.composite(logRecordProcessor1);
    assertThat(multiLogRecordProcessor).isSameAs(logRecordProcessor1);
  }

  @Test
  void twoLogRecordProcessor() {
    LogRecordProcessor multiLogRecordProcessor =
        LogRecordProcessor.composite(logRecordProcessor1, logRecordProcessor2);
    Context context = Context.current();
    multiLogRecordProcessor.onEmit(context, logRecord);
    verify(logRecordProcessor1).onEmit(same(context), same(logRecord));
    verify(logRecordProcessor2).onEmit(same(context), same(logRecord));

    multiLogRecordProcessor.forceFlush();
    verify(logRecordProcessor1).forceFlush();
    verify(logRecordProcessor2).forceFlush();

    multiLogRecordProcessor.shutdown();
    verify(logRecordProcessor1).shutdown();
    verify(logRecordProcessor2).shutdown();
  }
}
