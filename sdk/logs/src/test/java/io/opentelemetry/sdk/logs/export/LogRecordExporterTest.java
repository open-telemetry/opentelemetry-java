/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class LogRecordExporterTest {

  @Test
  void testNoop() {
    LogRecordExporter noop = LogRecordExporter.noop();
    assertNotNull(noop);
    assertSame(NoopLogRecordExporter.getInstance(), noop);
  }

  @Test
  void testComposite() {
    LogRecordExporter exp1 = mock();
    LogRecordExporter exp2 = mock();
    List<LogRecordData> logs = Collections.singletonList(mock());

    when(exp1.export(logs)).thenReturn(CompletableResultCode.ofSuccess());
    when(exp2.export(logs)).thenReturn(CompletableResultCode.ofSuccess());

    LogRecordExporter exporter = LogRecordExporter.composite(exp1, exp2);

    exporter.export(logs);

    verify(exp1).export(logs);
    verify(exp2).export(logs);
  }
}
