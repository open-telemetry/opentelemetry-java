/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import io.opentelemetry.sdk.extension.trace.jaeger.sampler.SamplingStrategyResponse.OperationSamplingStrategy;
import io.opentelemetry.sdk.extension.trace.jaeger.sampler.SamplingStrategyResponse.PerOperationSamplingStrategies;
import io.opentelemetry.sdk.extension.trace.jaeger.sampler.SamplingStrategyResponse.ProbabilisticSamplingStrategy;
import io.opentelemetry.sdk.extension.trace.jaeger.sampler.SamplingStrategyResponse.RateLimitingSamplingStrategy;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

class SamplingStrategyResponseTest {

  @Test
  void equalsVerifier() {
    // The builders always supply a value, so the fields are never null in practice.
    EqualsVerifier.forClass(RateLimitingSamplingStrategy.class).verify();
    EqualsVerifier.forClass(ProbabilisticSamplingStrategy.class).verify();
    EqualsVerifier.forClass(OperationSamplingStrategy.class).suppress(Warning.NULL_FIELDS).verify();
    EqualsVerifier.forClass(PerOperationSamplingStrategies.class)
        .suppress(Warning.NULL_FIELDS)
        .verify();
    EqualsVerifier.forClass(SamplingStrategyResponse.class).suppress(Warning.NULL_FIELDS).verify();
  }
}
