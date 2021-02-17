/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.processor;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.resources.Resource;

public interface LabelsProcessorFactory {
  static LabelsProcessorFactory noop() {
    return (resource, instrumentationLibraryInfo, descriptor) -> new NoopLabelsProcessor();
  }

  static LabelsProcessorFactory baggageExtractor(BaggageMetricsLabelsExtractor labelsExtractor) {
    return (resource, instrumentationLibraryInfo, descriptor) ->
        new BaggageLabelsProcessor(labelsExtractor);
  }

  /**
   * Returns a new {@link LabelsProcessorFactory}.
   *
   * @return new {@link LabelsProcessorFactory}
   */
  LabelsProcessor create(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor descriptor);
}
