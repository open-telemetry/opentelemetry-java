/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.sdk;

import io.opentelemetry.api.common.AttributeKey;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class AutoConfiguredOpenTelemetryTestingTest {

  @Test
  void sdkEnabled() {
    AutoConfiguredOpenTelemetryTesting testing =
        AutoConfiguredOpenTelemetryTesting.create(Collections.emptyMap());
    testing.getOpenTelemetry().getTracer("test").spanBuilder("test").startSpan().end();
    testing.assertTraces(trace -> trace.hasSize(1));
  }

  @Test
  void resourceAttributes() {
    AutoConfiguredOpenTelemetryTesting testing =
        AutoConfiguredOpenTelemetryTesting.create(
            Collections.singletonMap("otel.resource.attributes", "foo=bar"));
    testing.getOpenTelemetry().getTracer("test").spanBuilder("test").startSpan().end();
    testing.assertTraces(
        trace ->
            trace.hasSpansSatisfyingExactly(
                span ->
                    span.hasResourceSatisfying(
                        resource -> resource.hasAttribute(AttributeKey.stringKey("foo"), "bar"))));
  }
}
