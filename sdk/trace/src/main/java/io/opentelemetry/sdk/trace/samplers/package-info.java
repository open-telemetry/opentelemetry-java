/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * This package contains {@link io.opentelemetry.sdk.trace.samplers.Sampler}s for selecting traces
 * that are recorded and exported. <br>
 *
 * <p>Sampling is a mechanism to control the noise and overhead introduced by OpenTelemetry by
 * reducing the number of samples of traces collected and sent to the backend. See the <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/blob/1afab39e5658f807315abf2f3256809293bfd421/specification/trace/sdk.md#sampling">OpenTelemetry
 * specification</a> for more details. <br>
 *
 * <p>The following sampling strategies are provided here:
 *
 * <p>{@link io.opentelemetry.sdk.trace.samplers.Sampler#alwaysOff()} : This strategy will ensure
 * that no Spans are ever sent to the export pipeline.
 *
 * <p>{@link io.opentelemetry.sdk.trace.samplers.Sampler#alwaysOn()} : This strategy will ensure
 * that every Span will be sent to the export pipeline.
 *
 * <p>{@link io.opentelemetry.sdk.trace.samplers.Sampler#traceIdRatioBased(double)} : This strategy
 * will sample the provided fraction of Spans, deterministically based on the TraceId of the Spans.
 * This means that all spans from the a given trace will have the same sampling result.
 *
 * <p>{@link
 * io.opentelemetry.sdk.trace.samplers.Sampler#parentBased(io.opentelemetry.sdk.trace.samplers.Sampler)}
 * : This strategy will always use the sampled state of the parent span when deciding whether to
 * sample a Span or not. If the the Span has no parent, the provided "root" Sampler will be used for
 * that decision. The parent-based strategy is highly configurable, using the {@link
 * io.opentelemetry.sdk.trace.samplers.ParentBasedSamplerBuilder} which can be acquired from the
 * {@link
 * io.opentelemetry.sdk.trace.samplers.Sampler#parentBasedBuilder(io.opentelemetry.sdk.trace.samplers.Sampler)}
 * method.
 */
@ParametersAreNonnullByDefault
package io.opentelemetry.sdk.trace.samplers;

import javax.annotation.ParametersAreNonnullByDefault;
