/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.junit4;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

public class OpenTelemetryRuleTest {

  @Rule public OpenTelemetryRule otelTesting = OpenTelemetryRule.create();

  private static OpenTelemetry openTelemetryBeforeTest;

  // Class callbacks happen outside the rule so we can verify the restoration behavior in them.
  @BeforeClass
  public static void beforeTest() {
    openTelemetryBeforeTest = OpenTelemetry.get();
  }

  @AfterClass
  public static void afterTest() {
    assertThat(OpenTelemetry.get()).isSameAs(openTelemetryBeforeTest);
  }

  private Tracer tracer;

  @Before
  public void setup() {
    tracer = otelTesting.getOpenTelemetry().getTracer("test");
  }

  @Test
  public void exportSpan() {
    tracer.spanBuilder("test").startSpan().end();

    assertThat(otelTesting.getSpans())
        .singleElement()
        .satisfies(span -> assertThat(span.getName()).isEqualTo("test"));
    // Spans cleared between tests, not when retrieving
    assertThat(otelTesting.getSpans())
        .singleElement()
        .satisfies(span -> assertThat(span.getName()).isEqualTo("test"));
  }

  // We have two tests to verify spans get cleared up between tests.
  @Test
  public void exportSpanAgain() {
    tracer.spanBuilder("test").startSpan().end();

    assertThat(otelTesting.getSpans())
        .singleElement()
        .satisfies(span -> assertThat(span.getName()).isEqualTo("test"));
    // Spans cleared between tests, not when retrieving
    assertThat(otelTesting.getSpans())
        .singleElement()
        .satisfies(span -> assertThat(span.getName()).isEqualTo("test"));
  }
}
