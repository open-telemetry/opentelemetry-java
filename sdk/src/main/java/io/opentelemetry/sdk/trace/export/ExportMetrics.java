/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.trace.export;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.metrics.CounterLong;
import io.opentelemetry.metrics.GaugeLong;
import io.opentelemetry.metrics.LabelKey;
import java.util.Collections;
import java.util.List;

final class ExportMetrics {
  private static final String COMPONENT = "ot_proc_to_export";
  private static final List<LabelKey> LABEL_KEYS =
      Collections.singletonList(LabelKey.create("processor_name", "Name of the SpanProcessor."));

  // TODO: Add support for users to use a custom Meter.
  private static final CounterLong droppedSpans =
      OpenTelemetry.getMeter()
          .counterLongBuilder("dropped_spans")
          .setComponent(COMPONENT)
          .setLabelKeys(LABEL_KEYS)
          .setDescription("Number of spans dropped before the exporter pipeline.")
          .setUnit("1")
          .build();
  private static final CounterLong pushedSpans =
      OpenTelemetry.getMeter()
          .counterLongBuilder("pushed_spans")
          .setComponent(COMPONENT)
          .setLabelKeys(LABEL_KEYS)
          .setDescription("Number of spans pushed to the exporter pipeline.")
          .setUnit("1")
          .build();
  private static final GaugeLong batchedSpans =
      OpenTelemetry.getMeter()
          .gaugeLongBuilder("batched_spans")
          .setComponent(COMPONENT)
          .setLabelKeys(LABEL_KEYS)
          .setDescription("Current number of spans batched for the exporter pipeline.")
          .setUnit("1")
          .build();

  static CounterLong getDroppedSpans() {
    return droppedSpans;
  }

  static CounterLong getPushedSpans() {
    return pushedSpans;
  }

  static GaugeLong getBatchedSpans() {
    return batchedSpans;
  }

  private ExportMetrics() {}
}
