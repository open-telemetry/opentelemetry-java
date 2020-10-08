/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Utilities that allows different tracing services to export recorded data for sampled spans in
 * their own format.
 *
 * <h2>Contents</h2>
 *
 * <ul>
 *   <li>{@link io.opentelemetry.sdk.trace.export.SpanExporter}
 *   <li>{@link io.opentelemetry.sdk.trace.export.SimpleSpanProcessor}
 *   <li>{@link io.opentelemetry.sdk.trace.export.BatchSpanProcessor}
 *   <li>{@link io.opentelemetry.sdk.trace.export.MultiSpanExporter}
 * </ul>
 *
 * <h2>Default values for {@link io.opentelemetry.sdk.trace.export.SimpleSpanProcessor}</h2>
 *
 * <ul>
 *   <li>{@code REPORT_ONLY_SAMPLED: true} *
 * </ul>
 *
 * <p>Configuration options for {@link io.opentelemetry.sdk.trace.export.SimpleSpanProcessor} can be
 * read from system properties, environment variables, or {@link java.util.Properties} objects.
 *
 * <p>For system properties and {@link java.util.Properties} objects, {@link
 * io.opentelemetry.sdk.trace.export.SimpleSpanProcessor} will look for the following names:
 *
 * <ul>
 *   <li>{@code otel.ssp.export.sampled}: sets whether only sampled spans should be exported.
 * </ul>
 *
 * <p>For environment variables, {@link io.opentelemetry.sdk.trace.export.SimpleSpanProcessor} will
 * look for the following names:
 *
 * <ul>
 *   <li>{@code OTEL_SSP_EXPORT_SAMPLED}: sets whether only sampled spans should be exported.
 * </ul>
 *
 * <h2>Default values for {@link io.opentelemetry.sdk.trace.export.BatchSpanProcessor}</h2>
 *
 * <ul>
 *   <li>{@code SCHEDULE_DELAY_MILLIS: 5000}
 *   <li>{@code MAX_QUEUE_SIZE: 2048}
 *   <li>{@code MAX_EXPORT_BATCH_SIZE: 512}
 *   <li>{@code EXPORT_TIMEOUT_MILLIS: 30_000}
 *   <li>{@code REPORT_ONLY_SAMPLED: true}
 * </ul>
 *
 * <p>Configuration options for {@link io.opentelemetry.sdk.trace.export.BatchSpanProcessor} can be
 * read from system properties, environment variables, or {@link java.util.Properties} objects.
 *
 * <p>For system properties and {@link java.util.Properties} objects, {@link
 * io.opentelemetry.sdk.trace.export.BatchSpanProcessor} will look for the following names:
 *
 * <ul>
 *   <li>{@code otel.bsp.schedule.delay.millis}: sets the delay interval between two consecutive
 *       exports.
 *   <li>{@code otel.bsp.max.queue.size}: sets the maximum queue size.
 *   <li>{@code otel.bsp.max.export.batch.size}: sets the maximum batch size.
 *   <li>{@code otel.bsp.export.timeout.millis}: sets the maximum allowed time to export data.
 *   <li>{@code otel.bsp.export.sampled}: sets whether only sampled spans should be exported.
 * </ul>
 *
 * <p>For environment variables, {@link io.opentelemetry.sdk.trace.export.BatchSpanProcessor} will
 * look for the following names:
 *
 * <ul>
 *   <li>{@code OTEL_BSP_SCHEDULE_DELAY_MILLIS}: sets the delay interval between two consecutive
 *       exports.
 *   <li>{@code OTEL_BSP_MAX_QUEUE_SIZE}: sets the maximum queue size.
 *   <li>{@code OTEL_BSP_MAX_EXPORT_BATCH_SIZE}: sets the maximum batch size.
 *   <li>{@code OTEL_BSP_EXPORT_TIMEOUT_MILLIS}: sets the maximum allowed time to export data.
 *   <li>{@code OTEL_BSP_EXPORT_SAMPLED}: sets whether only sampled spans should be exported.
 * </ul>
 */
@ParametersAreNonnullByDefault
package io.opentelemetry.sdk.trace.export;

import javax.annotation.ParametersAreNonnullByDefault;
