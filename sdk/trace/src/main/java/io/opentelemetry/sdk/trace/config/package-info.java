/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Classes that holds global trace parameters
 *
 * <h2>Contents</h2>
 *
 * <ul>
 *   <li>{@link io.opentelemetry.sdk.trace.config.TraceConfig}
 * </ul>
 *
 * <h2>Default values for {@link io.opentelemetry.sdk.trace.config.TraceConfig}</h2>
 *
 * <ul>
 *   <li>{@code SAMPLER: Samplers.alwaysOn()}
 *   <li>{@code SPAN_MAX_NUM_ATTRIBUTES: 32}
 *   <li>{@code SPAN_MAX_NUM_EVENTS: 128}
 *   <li>{@code SPAN_MAX_NUM_LINKS: 32}
 *   <li>{@code SPAN_MAX_NUM_ATTRIBUTES_PER_EVENT: 32}
 *   <li>{@code SPAN_MAX_NUM_ATTRIBUTES_PER_LINK: 32}
 * </ul>
 *
 * <p>Configuration options for {@link io.opentelemetry.sdk.trace.config.TraceConfig} can be read
 * from system properties, environment variables, or {@link java.util.Properties} objects.
 *
 * <p>For system Properties and {@link java.util.Properties} objects, {@link
 * io.opentelemetry.sdk.trace.config.TraceConfig} will look for the following names:
 *
 * <ul>
 *   <li>{@code otel.config.sampler.probability}: to set the global default sampler which is used
 *       when constructing a new {@code Span}.
 *   <li>{@code otel.span.attribute.count.limit}: to set the global default max number of attributes
 *       per {@code Span}.
 *   <li>{@code otel.span.event.count.limit}: to set the global default max number of events per
 *       {@code Span}.
 *   <li>{@code otel.span.link.count.limit}: to set the global default max number of links per
 *       {@code Span}.
 *   <li>{@code otel.config.max.event.attrs}: to set the global default max number of attributes per
 *       event.
 *   <li>{@code otel.config.max.link.attrs}: to set the global default max number of attributes per
 *       link.
 * </ul>
 *
 * <p>For environment variable, {@link io.opentelemetry.sdk.trace.config.TraceConfig} will look for
 * the following names:
 *
 * <ul>
 *   <li>{@code OTEL_CONFIG_SAMPLER_PROBABILITY}: to set the global default sampler which is used
 *       when constructing a new {@code Span}.
 *   <li>{@code OTEL_SPAN_ATTRIBUTE_COUNT_LIMIT}: to set the global default max number of attributes
 *       per {@code Span}.
 *   <li>{@code OTEL_SPAN_EVENT_COUNT_LIMIT}: to set the global default max number of events per
 *       {@code Span}.
 *   <li>{@code OTEL_SPAN_LINK_COUNT_LIMIT}: to set the global default max number of links entries
 *       per {@code Span}.
 *   <li>{@code OTEL_CONFIG_MAX_EVENT_ATTRS}: to set the global default max number of attributes per
 *       event.
 *   <li>{@code OTEL_CONFIG_MAX_LINK_ATTRS}: to set the global default max number of attributes per
 *       link.
 * </ul>
 */
@ParametersAreNonnullByDefault
package io.opentelemetry.sdk.trace.config;

import javax.annotation.ParametersAreNonnullByDefault;
