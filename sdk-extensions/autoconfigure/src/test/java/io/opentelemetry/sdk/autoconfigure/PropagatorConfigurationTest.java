/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class PropagatorConfigurationTest {

  @Test
  void defaultPropagators() {
    ContextPropagators contextPropagators =
        PropagatorConfiguration.configurePropagators(
            DefaultConfigProperties.createForTest(Collections.emptyMap()),
            PropagatorConfiguration.class.getClassLoader(),
            (a, unused) -> a);

    assertThat(contextPropagators.getTextMapPropagator().fields())
        .containsExactlyInAnyOrder("traceparent", "tracestate", "baggage");
  }

  @Test
  void configurePropagators_none() {
    ContextPropagators contextPropagators =
        PropagatorConfiguration.configurePropagators(
            DefaultConfigProperties.createForTest(
                Collections.singletonMap("otel.propagators", "none")),
            PropagatorConfiguration.class.getClassLoader(),
            (a, unused) -> a);

    assertThat(contextPropagators.getTextMapPropagator().fields()).isEmpty();
  }

  @Test
  void configurePropagators_none_withOthers() {
    assertThatThrownBy(
            () ->
                PropagatorConfiguration.configurePropagators(
                    DefaultConfigProperties.createForTest(
                        Collections.singletonMap("otel.propagators", "none,blather")),
                    PropagatorConfiguration.class.getClassLoader(),
                    (a, unused) -> a))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("otel.propagators contains 'none' along with other propagators");
  }

  @Test
  void configurePropagators_NotOnClasspath() {
    assertThatThrownBy(
            () ->
                PropagatorConfiguration.configurePropagators(
                    DefaultConfigProperties.createForTest(
                        Collections.singletonMap("otel.propagators", "b3")),
                    PropagatorConfiguration.class.getClassLoader(),
                    (a, config) -> a))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unrecognized value for otel.propagators: b3");
  }
}
