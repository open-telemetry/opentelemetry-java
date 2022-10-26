/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.opentelemetry.context.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NoopLogRecordProcessorTest {

  @Mock private ReadWriteLogRecord logRecord;

  @Test
  void noCrash() {
    LogRecordProcessor logRecordProcessor = NoopLogRecordProcessor.getInstance();
    logRecordProcessor.onEmit(Context.current(), logRecord);
    assertThat(logRecordProcessor.forceFlush().isSuccess()).isEqualTo(true);
    assertThat(logRecordProcessor.shutdown().isSuccess()).isEqualTo(true);
  }
}
