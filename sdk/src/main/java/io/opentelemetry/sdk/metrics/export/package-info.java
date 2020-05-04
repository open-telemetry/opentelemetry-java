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

/**
 * Utilities that allow to export metrics to the OpenTelemetry exporters.
 *
 * <h2>Contents</h2>
 *
 * <ul>
 *   <li>{@link io.opentelemetry.sdk.metrics.export.IntervalMetricReader}
 *   <li>{@link io.opentelemetry.sdk.metrics.export.MetricExporter}
 *   <li>{@link io.opentelemetry.sdk.metrics.export.MetricProducer}
 * </ul>
 *
 * <p>Values for {@link io.opentelemetry.sdk.metrics.export.IntervalMetricReader} can be read from
 * system properties, environment variables, or {@link java.util.Properties} objects.
 *
 * <p>For System Properties and {@link java.util.Properties} objects, {@link
 * io.opentelemetry.sdk.metrics.export.IntervalMetricReader} will look for the following names:
 *
 * <ul>
 *   <li>{@code otel.imr.export.interval}: sets the export interval between pushes to the exporter.
 * </ul>
 *
 * <p>For Environment Variable, {@link io.opentelemetry.sdk.metrics.export.IntervalMetricReader}
 * will look for the following names:
 *
 * <ul>
 *   <li>{@code OTEL_IMR_EXPORT_INTERVAL}: sets the export interval between pushes to the exporter.
 * </ul>
 */
package io.opentelemetry.sdk.metrics.export;
