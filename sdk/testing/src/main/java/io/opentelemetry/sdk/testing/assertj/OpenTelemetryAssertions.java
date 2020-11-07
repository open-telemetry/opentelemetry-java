/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import io.opentelemetry.api.common.ReadableAttributes;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.assertj.core.api.Assertions;

/**
 * Entry point for assertion methods for OpenTelemetry types. To use type-specific assertions,
 * static import any {@code assertThat} method in this class instead of {@code
 * Assertions.assertThat}.
 */
public class OpenTelemetryAssertions extends Assertions {

  /** Returns an assertion for {@link ReadableAttributes}. */
  public static AttributesAssert assertThat(ReadableAttributes attributes) {
    return new AttributesAssert(attributes);
  }

  /** Returns an assertion for {@link SpanDataAssert}. */
  public static SpanDataAssert assertThat(SpanData spanData) {
    return new SpanDataAssert(spanData);
  }

  protected OpenTelemetryAssertions() {}
}
