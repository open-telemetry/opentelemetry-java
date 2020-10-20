/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
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
 * <p>Configuration options for {@link io.opentelemetry.sdk.metrics.export.IntervalMetricReader} can
 * be read from system properties, environment variables, or {@link java.util.Properties} objects.
 *
 * <p>For system properties and {@link java.util.Properties} objects, {@link
 * io.opentelemetry.sdk.metrics.export.IntervalMetricReader} will look for the following names:
 *
 * <ul>
 *   <li>{@code otel.imr.export.interval}: sets the export interval between pushes to the exporter.
 * </ul>
 *
 * <p>For environment variables, {@link io.opentelemetry.sdk.metrics.export.IntervalMetricReader}
 * will look for the following names:
 *
 * <ul>
 *   <li>{@code OTEL_IMR_EXPORT_INTERVAL}: sets the export interval between pushes to the exporter.
 * </ul>
 */
@ParametersAreNonnullByDefault
package io.opentelemetry.sdk.metrics.export;

import javax.annotation.ParametersAreNonnullByDefault;
