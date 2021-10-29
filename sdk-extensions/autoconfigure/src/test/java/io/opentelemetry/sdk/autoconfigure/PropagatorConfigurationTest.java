/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.context.propagation.ContextPropagators;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class PropagatorConfigurationTest {

  @Test
  void defaultPropagators() {
    ContextPropagators contextPropagators =
        PropagatorConfiguration.configurePropagators(
            DefaultConfigProperties.createForTest(Collections.emptyMap()), (a, unused) -> a);

    assertThat(contextPropagators.getTextMapPropagator().fields())
        .containsExactlyInAnyOrder("traceparent", "tracestate", "baggage");
  }
}
