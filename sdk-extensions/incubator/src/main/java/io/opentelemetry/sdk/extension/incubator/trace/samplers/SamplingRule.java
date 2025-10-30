/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.samplers;

/** A rule which returns a {@link ComposableSampler} to use when a predicate matches. */
interface SamplingRule {

  /** The {@link SamplingPredicate} which indicates whether to use {@link #sampler()}. */
  SamplingPredicate predicate();

  /** The {@link ComposableSampler} to use when the rule matches. */
  ComposableSampler sampler();
}
