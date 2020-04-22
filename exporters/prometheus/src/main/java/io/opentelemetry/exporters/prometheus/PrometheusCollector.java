/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.exporters.prometheus;

import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.prometheus.client.Collector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class PrometheusCollector extends Collector {
  private final MetricProducer metricProducer;

  private PrometheusCollector(MetricProducer metricProducer) {
    this.metricProducer = metricProducer;
  }

  @Override
  public List<MetricFamilySamples> collect() {
    Collection<MetricData> allMetrics = metricProducer.getAllMetrics();
    List<MetricFamilySamples> allSamples = new ArrayList<>(allMetrics.size());
    for (MetricData metricData : allMetrics) {
      allSamples.add(MetricAdapter.toMetricFamilySamples(metricData));
    }
    return allSamples;
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static Builder newBuilder() {
    return new Builder();
  }

  /** Builder utility for this exporter. */
  public static class Builder {
    private MetricProducer metricProducer;

    /**
     * Sets the metric producer for the collector. Required.
     *
     * @param metricProducer the {@link MetricProducer} to use.
     * @return this builder's instance.
     */
    public Builder setMetricProducer(MetricProducer metricProducer) {
      this.metricProducer = metricProducer;
      return this;
    }

    /**
     * Constructs a new instance of the {@link Collector} based on the builder's values.
     *
     * @return a new {@code Collector} based on the builder's values.
     */
    public PrometheusCollector build() {
      return new PrometheusCollector(Objects.requireNonNull(metricProducer, "metricProducer"));
    }

    /**
     * Constructs a new instance of the {@link Collector} based on the builder's values and
     * registers it to Prometheus {@link io.prometheus.client.CollectorRegistry#defaultRegistry}.
     *
     * @return a new {@code Collector} based on the builder's values.
     */
    public PrometheusCollector buildAndRegister() {
      return build().register();
    }
  }
}
