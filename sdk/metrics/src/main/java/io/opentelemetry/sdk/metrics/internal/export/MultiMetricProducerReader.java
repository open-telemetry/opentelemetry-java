/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.export;

import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.CollectionRegistration;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Helper class allowing {@link MetricReader} implementations to easily read from multiple {@link
 * MetricProducer}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class MultiMetricProducerReader {

  @Nullable private volatile CollectionRegistration collectionRegistration;

  private MultiMetricProducerReader() {}

  public static MultiMetricProducerReader create() {
    return new MultiMetricProducerReader();
  }

  public void register(CollectionRegistration collectionRegistration) {
    this.collectionRegistration = collectionRegistration;
  }

  /** Read from all registered {@link MetricProducer}. */
  public Collection<MetricData> readAll() {
    CollectionRegistration currentCollectionRegister = collectionRegistration;
    if (currentCollectionRegister == null) {
      return Collections.emptyList();
    }
    List<MetricProducer> metricProducers = currentCollectionRegister.getMetricProducers();
    if (metricProducers.size() == 0) {
      return Collections.emptyList();
    }
    Resource resource = currentCollectionRegister.getResource();
    if (metricProducers.size() == 1) {
      return metricProducers.get(0).produce(resource);
    }
    List<MetricData> metricData = new ArrayList<>();
    for (MetricProducer metricProducer : metricProducers) {
      metricData.addAll(metricProducer.produce(resource));
    }
    return Collections.unmodifiableList(metricData);
  }
}
