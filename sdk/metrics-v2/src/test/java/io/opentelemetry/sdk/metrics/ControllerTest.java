/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.mockito.Mockito.mock;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

class ControllerTest {
  @Test
  void controllerLoop() {
    Accumulator accumulator = mock(Accumulator.class);
    Processor processor = mock(Processor.class);
    MetricExporter exporter = mock(MetricExporter.class);
    Controller controller = new Controller(accumulator, processor, exporter);

    List<MetricData> exportedData =
        Collections.singletonList(
            MetricData.create(
                mock(Resource.class),
                mock(InstrumentationLibraryInfo.class),
                "test",
                "testDescription",
                "unit",
                MetricData.Type.SUMMARY,
                Collections.emptyList()));
    Mockito.when(processor.finish()).thenReturn(exportedData);

    // manually run the cycle for testing
    controller.runOneCycle();

    InOrder inOrder = Mockito.inOrder(accumulator, processor, exporter);
    inOrder.verify(processor).start();
    inOrder.verify(accumulator).collectAndSendTo(processor);
    inOrder.verify(processor).finish();
    inOrder.verify(exporter).export(exportedData);
  }
}
