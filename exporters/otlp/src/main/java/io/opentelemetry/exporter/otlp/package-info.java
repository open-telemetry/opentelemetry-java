/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * OpenTelemetry exporter which sends span and metric data to OpenTelemetry collector via gRPC.
 *
 * <h2>Contents</h2>
 *
 * <ul>
 *   <li>{@link io.opentelemetry.sdk.extension.otproto.CommonAdapter}
 *   <li>{@link io.opentelemetry.sdk.extension.otproto.MetricAdapter}
 *   <li>{@link io.opentelemetry.exporter.otlp.OtlpGrpcMetricExporter}
 *   <li>{@link io.opentelemetry.exporter.otlp.OtlpGrpcSpanExporter}
 *   <li>{@link io.opentelemetry.sdk.extension.otproto.ResourceAdapter}
 *   <li>{@link io.opentelemetry.sdk.extension.otproto.SpanAdapter}
 * </ul>
 *
 * <p>Configuration options for {@link io.opentelemetry.exporter.otlp.OtlpGrpcMetricExporter} and
 * {@link io.opentelemetry.exporter.otlp.OtlpGrpcSpanExporter} can be read from system properties,
 * environment variables, or {@link java.util.Properties} objects.
 *
 * <h2>{@link io.opentelemetry.exporter.otlp.OtlpGrpcMetricExporter}</h2>
 *
 * <p>For system properties and {@link java.util.Properties} objects, {@link
 * io.opentelemetry.exporter.otlp.OtlpGrpcMetricExporter} will look for the following names:
 *
 * <ul>
 *   <li>{@code otel.exporter.otlp.metric.timeout}: to set the max waiting time allowed to send each
 *       metric batch.
 *   <li>{@code otel.exporter.otlp.metric.endpoint}: to set the endpoint to connect to.
 *   <li>{@code otel.exporter.otlp.metric.insecure}: whether to enable client transport security for
 *       the connection.
 *   <li>{@code otel.exporter.otlp.metric.headers}: the headers associated with the requests.
 * </ul>
 *
 * <p>For environment variables, {@link io.opentelemetry.exporter.otlp.OtlpGrpcMetricExporter} will
 * look for the following names:
 *
 * <ul>
 *   <li>{@code OTEL_EXPORTER_OTLP_METRIC_TIMEOUT}: to set the max waiting time allowed to send each
 *       * span batch. *
 *   <li>{@code OTEL_EXPORTER_OTLP_METRIC_ENDPOINT}: to set the endpoint to connect to. *
 *   <li>{@code OTEL_EXPORTER_OTLP_METRIC_INSECURE}: whether to enable client transport security for
 *       * the connection. *
 *   <li>{@code OTEL_EXPORTER_OTLP_METRIC_HEADERS}: the headers associated with the requests. *
 * </ul>
 *
 * In both cases, if a property is missing, the name without "metric" is used to resolve the value.
 *
 * <h2>{@link io.opentelemetry.exporter.otlp.OtlpGrpcSpanExporter}</h2>
 *
 * <p>For system properties and {@link java.util.Properties} objects, {@link
 * io.opentelemetry.exporter.otlp.OtlpGrpcSpanExporter} will look for the following names:
 *
 * <ul>
 *   <li>{@code otel.exporter.otlp.span.timeout}: to set the max waiting time allowed to send each
 *       span batch.
 *   <li>{@code otel.exporter.otlp.span.endpoint}: to set the endpoint to connect to.
 *   <li>{@code otel.exporter.otlp.span.insecure}: whether to enable client transport security for
 *       the connection.
 *   <li>{@code otel.exporter.otlp.span.headers}: the headers associated with the requests.
 * </ul>
 *
 * <p>For environment variables, {@link io.opentelemetry.exporter.otlp.OtlpGrpcSpanExporter} will
 * look for the following names:
 *
 * <ul>
 *   <li>{@code OTEL_EXPORTER_OTLP_SPAN_TIMEOUT}: to set the max waiting time allowed to send each
 *       span batch.
 *   <li>{@code OTEL_EXPORTER_OTLP_SPAN_ENDPOINT}: to set the endpoint to connect to.
 *   <li>{@code OTEL_EXPORTER_OTLP_SPAN_INSECURE}: whether to enable client transport security for
 *       the connection.
 *   <li>{@code OTEL_EXPORTER_OTLP_SPAN_HEADERS}: the headers associated with the requests.
 * </ul>
 *
 * In both cases, if a property is missing, the name without "span" is used to resolve the value.
 */
package io.opentelemetry.exporter.otlp;
