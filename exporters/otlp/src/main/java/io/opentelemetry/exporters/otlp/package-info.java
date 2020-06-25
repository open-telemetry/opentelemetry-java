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

/**
 * OpenTelemetry exporter which sends span and metric data to OpenTelemetry collector via gRPC.
 *
 * <h2>Contents</h2>
 *
 * <ul>
 *   <li>{@link io.opentelemetry.exporters.otlp.CommonAdapter}
 *   <li>{@link io.opentelemetry.exporters.otlp.MetricAdapter}
 *   <li>{@link io.opentelemetry.exporters.otlp.OtlpGrpcMetricExporter}
 *   <li>{@link io.opentelemetry.exporters.otlp.OtlpGrpcSpanExporter}
 *   <li>{@link io.opentelemetry.exporters.otlp.ResourceAdapter}
 *   <li>{@link io.opentelemetry.exporters.otlp.SpanAdapter}
 * </ul>
 *
 * <p>Configuration options for {@link io.opentelemetry.exporters.otlp.OtlpGrpcMetricExporter} and
 * {@link io.opentelemetry.exporters.otlp.OtlpGrpcSpanExporter} can be read from system properties,
 * environment variables, or {@link java.util.Properties} objects.
 *
 * <h2>{@link io.opentelemetry.exporters.otlp.OtlpGrpcMetricExporter}</h2>
 *
 * <p>For system properties and {@link java.util.Properties} objects, {@link
 * io.opentelemetry.exporters.otlp.OtlpGrpcMetricExporter} will look for the following names:
 *
 * <ul>
 *   <li>{@code otel.otlp.metric.timeout}: to set the max waiting time allowed to send each metric
 *       batch.
 *   <li>{@code otel.otlp.endpoint}: to set the endpoint to connect to.
 *   <li>{@code otel.otlp.use.tls}: to set use or not TLS.
 *   <li>{@code otel.otlp.metadata} to set key-value pairs separated by semicolon to pass as request
 *       metadata.
 * </ul>
 *
 * <p>For environment variables, {@link io.opentelemetry.exporters.otlp.OtlpGrpcMetricExporter} will
 * look for the following names:
 *
 * <ul>
 *   <li>{@code OTEL_OTLP_METRIC_TIMEOUT}: to set the max waiting time allowed to send each metric
 *       batch.
 *   <li>{@code OTEL_OTLP_ENDPOINT}: to set the endpoint to connect to.
 *   <li>{@code OTEL_OTLP_USE_TLS}: to set use or not TLS.
 *   <li>{@code OTEL_OTLP_METADATA}: to set key-value pairs separated by semicolon to pass as
 *       request metadata.
 * </ul>
 *
 * <h2>{@link io.opentelemetry.exporters.otlp.OtlpGrpcSpanExporter}</h2>
 *
 * <p>For system properties and {@link java.util.Properties} objects, {@link
 * io.opentelemetry.exporters.otlp.OtlpGrpcSpanExporter} will look for the following names:
 *
 * <ul>
 *   <li>{@code otel.otlp.span.timeout}: to set the max waiting time allowed to send each span
 *       batch.
 * </ul>
 *
 * <p>For environment variables, {@link io.opentelemetry.exporters.otlp.OtlpGrpcSpanExporter} will
 * look for the following names:
 *
 * <ul>
 *   <li>{@code OTEL_OTLP_SPAN_TIMEOUT}: to set the max waiting time allowed to send each span
 *       batch.
 * </ul>
 */
package io.opentelemetry.exporters.otlp;
