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
 * Utilities that allows different tracing services to export recorded data for sampled spans in
 * their own format.
 *
 * <h2>Contents</h2>
 *
 * <ul>
 *   <li>{@link io.opentelemetry.sdk.trace.export.SpanExporter}
 *   <li>{@link io.opentelemetry.sdk.trace.export.SimpleSpansProcessor}
 *   <li>{@link io.opentelemetry.sdk.trace.export.BatchSpansProcessor}
 *   <li>{@link io.opentelemetry.sdk.trace.export.MultiSpanExporter}
 * </ul>
 *
 * <h2>Default values for {@link io.opentelemetry.sdk.trace.export.BatchSpansProcessor.Config}</h2>
 *
 * <ul>
 *   <li>{@code SCHEDULE_DELAY_MILLIS: 5000}
 *   <li>{@code MAX_QUEUE_SIZE: 2048}
 *   <li>{@code MAX_EXPORT_BATCH_SIZE: 512}
 *   <li>{@code EXPORT_TIMEOUT_MILLIS: 30_000}
 *   <li>{@code REPORT_ONLY_SAMPLED: true}
 * </ul>
 *
 * <p>Values for {@link io.opentelemetry.sdk.trace.export.BatchSpansProcessor.Config} can be read
 * from system properties, environment variables, or {@link java.util.Properties} objects.
 *
 * <p>For System Properties and {@link java.util.Properties} objects, {@link
 * io.opentelemetry.sdk.trace.export.BatchSpansProcessor.Config} will look for the following names:
 *
 * <ul>
 *   <li>{@code otel.bsp.schedule.delay}: sets the delay interval between two consecutive exports.
 *   <li>{@code otel.bsp.max.queue}: sets the maximum queue size.
 *   <li>{@code otel.bsp.max.export.batch}: sets the maximum batch size.
 *   <li>{@code otel.bsp.export.timeout}: sets the maximum allowed time to export data.
 *   <li>{@code otel.bsp.export.sampled}: sets whether only sampled spans should be exported.
 * </ul>
 *
 * <p>For Environment Variable, {@link io.opentelemetry.sdk.trace.export.BatchSpansProcessor.Config}
 * will look for the following names:
 *
 * <ul>
 *   <li>{@code OTEL_BSP_SCHEDULE_DELAY}: sets the delay interval between two consecutive exports.
 *   <li>{@code OTEL_BSP_MAX_QUEUE}: sets the maximum queue size.
 *   <li>{@code OTEL_BSP_MAX_EXPORT_BATCH}: sets the maximum batch size.
 *   <li>{@code OTEL_BSP_EXPORT_TIMEOUT}: sets the maximum allowed time to export data.
 *   <li>{@code OTEL_BSP_EXPORT_SAMPLED}: sets whether only sampled spans should be exported.
 * </ul>
 */
package io.opentelemetry.sdk.trace.export;
