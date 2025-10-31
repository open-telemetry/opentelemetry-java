/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.samplers;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.LinkData;
import java.util.List;

/**
 * A predicate for a composable sampler, indicating whether a set of sampling arguments matches.
 *
 * <p>While this can be implemented with lambda expressions, it is recommended to implement {@link
 * Object#toString()} as well with an explanation of the predicate for rendering in {@link
 * io.opentelemetry.sdk.trace.samplers.Sampler#getDescription()}.
 */
@FunctionalInterface
public interface SamplingPredicate {
  /** Returns whether this {@link SamplingPredicate} matches the given sampling arguments. */
  boolean matches(
      Context parentContext,
      String traceId,
      String name,
      SpanKind spanKind,
      Attributes attributes,
      List<LinkData> parentLinks);
}
