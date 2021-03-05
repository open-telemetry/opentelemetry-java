/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.processor;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.api.metrics.common.LabelsBuilder;
import io.opentelemetry.context.Context;
import org.junit.jupiter.api.Test;

public class BaggageLabelsProcessorTest {

  @Test
  void testLabelsExtractedAndAdded() {
    Labels extractedLabels = Labels.of("aa", "bb");
    BaggageMetricsLabelsExtractor extractor = ctx -> extractedLabels;
    BaggageLabelsProcessor labelsProcessor = new BaggageLabelsProcessor(extractor);
    Labels originalLabels = Labels.of("a", "b");
    Labels newLabels = labelsProcessor.onLabelsBound(Context.current(), originalLabels);

    LabelsBuilder mergedLabels = newLabels.toBuilder();
    extractedLabels.asMap().forEach((k, v) -> newLabels.toBuilder().put(k, v));
    assertThat(newLabels).isEqualTo(mergedLabels.build());
  }
}
