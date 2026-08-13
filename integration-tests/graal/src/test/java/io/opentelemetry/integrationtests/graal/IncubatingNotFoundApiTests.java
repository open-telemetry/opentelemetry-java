/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.integrationtests.graal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.TracerProvider;
import org.junit.jupiter.api.Test;

class IncubatingNotFoundApiTests {
  @Test
  void incubatingApiIsNotFoundViaReflection() {
    // The graal module deliberately excludes :api:incubator, so the noop instances must come from
    // the stable api packages, not io.opentelemetry.api.incubator.*. An isInstanceOf check against
    // the stable types cannot detect a regression here because the incubator Extended* types
    // subtype the stable types; assert on the runtime class package instead, since the Extended*
    // types are not on this module's classpath to reference directly.
    assertThat(LoggerProvider.noop().get("test").getClass().getName())
        .doesNotStartWith("io.opentelemetry.api.incubator.");
    assertThat(TracerProvider.noop().get("test").getClass().getName())
        .doesNotStartWith("io.opentelemetry.api.incubator.");
    assertThat(MeterProvider.noop().get("test").counterBuilder("test").getClass().getName())
        .doesNotStartWith("io.opentelemetry.api.incubator.");
  }
}
