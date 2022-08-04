/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.testing.logs.TestLogData;
import org.junit.jupiter.api.Test;

class NoopLogProcessorTest {

  @Test
  void noCrash() {
    LogProcessor logProcessor = NoopLogProcessor.getInstance();
    logProcessor.emit(TestLogData.builder().setSeverity(Severity.DEBUG).setBody("message").build());
    assertThat(logProcessor.forceFlush().isSuccess()).isEqualTo(true);
    assertThat(logProcessor.shutdown().isSuccess()).isEqualTo(true);
  }
}
