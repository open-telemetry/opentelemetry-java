/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class SpanExporterTest {

  @Test
  void testNoop() {
    SpanExporter exporter = SpanExporter.noop();
    assertNotNull(exporter);
    assertSame(exporter, NoopSpanExporter.getInstance());
  }

  @Test
  void testComposite() {
    SpanExporter exp1 = mock();
    SpanExporter exp2 = mock();

    Collection<SpanData> spans = Collections.singletonList(mock(SpanData.class));

    when(exp1.export(spans)).thenReturn(CompletableResultCode.ofSuccess());
    when(exp2.export(spans)).thenReturn(CompletableResultCode.ofSuccess());

    SpanExporter exporter = SpanExporter.composite(exp1, exp2);

    exporter.export(spans);
    verify(exp1).export(spans);
    verify(exp2).export(spans);
  }
}
