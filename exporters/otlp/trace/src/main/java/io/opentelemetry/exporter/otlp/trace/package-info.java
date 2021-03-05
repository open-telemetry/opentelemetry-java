/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * OpenTelemetry exporter which sends span data to OpenTelemetry collector via gRPC.
 *
 * <p>Configuration options for {@link io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter}
 * can be read from system properties, environment variables, or {@link java.util.Properties}
 * objects.
 *
 * <p>For system properties and {@link java.util.Properties} objects, {@link
 * io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter} will look for the following names:
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
 * <p>For environment variables, {@link io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter}
 * will look for the following names:
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
@ParametersAreNonnullByDefault
package io.opentelemetry.exporter.otlp.trace;

import javax.annotation.ParametersAreNonnullByDefault;
