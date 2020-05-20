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
 *   <li>{@code otel.config.sampler.probability}: to set the global default sampler for traces.
 *   <li>{@code otel.config.max.attrs}: to set the global default max number of attributes per
 *       {@link io.opentelemetry.trace.Span}.
 *   <li>{@code otel.config.max.events}: to set the global default max number of {@link
 *       io.opentelemetry.trace.Event}s per {@link io.opentelemetry.trace.Span}.
 *   <li>{@code otel.config.max.links}: to set the global default max number of {@link
 *       io.opentelemetry.trace.Link} entries per {@link io.opentelemetry.trace.Span}.
 *   <li>{@code otel.config.max.event.attrs}: to set the global default max number of attributes per
 *       {@link io.opentelemetry.trace.Event}.
 *   <li>{@code otel.config.max.link.attrs}: to set the global default max number of attributes per
 *       {@link io.opentelemetry.trace.Link}.
 * </ul>
 *
 * <p>For environment variable, {@link io.opentelemetry.sdk.trace.config.TraceConfig} will look for
 * the following names:
 *
 * <ul>
 *   <li>{@code OTEL_CONFIG_SAMPLER_PROBABILITY}: to set the global default sampler for traces.
 *   <li>{@code OTEL_CONFIG_MAX_ATTRS}: to set the global default max number of attributes per
 *       {@link io.opentelemetry.trace.Span}.
 *   <li>{@code OTEL_CONFIG_MAX_EVENTS}: to set the global default max number of {@link
 *       io.opentelemetry.trace.Event}s per {@link io.opentelemetry.trace.Span}.
 *   <li>{@code OTEL_CONFIG_MAX_LINKS}: to set the global default max number of {@link
 *       io.opentelemetry.trace.Link} entries per {@link io.opentelemetry.trace.Span}.
 *   <li>{@code OTEL_CONFIG_MAX_EVENT_ATTRS}: to set the global default max number of attributes per
 *       {@link io.opentelemetry.trace.Event}.
 *   <li>{@code OTEL_CONFIG_MAX_LINK_ATTRS}: to set the global default max number of attributes per
 *       {@link io.opentelemetry.trace.Link}.
 * </ul>
 */
package io.opentelemetry.sdk.trace.config;
