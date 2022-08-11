/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NoopLogProcessorTest {

  @Mock private ReadWriteLogRecord logRecord;

  @Test
  void noCrash() {
    LogProcessor logProcessor = NoopLogProcessor.getInstance();
    logProcessor.onEmit(logRecord);
    assertThat(logProcessor.forceFlush().isSuccess()).isEqualTo(true);
    assertThat(logProcessor.shutdown().isSuccess()).isEqualTo(true);
  }
}
