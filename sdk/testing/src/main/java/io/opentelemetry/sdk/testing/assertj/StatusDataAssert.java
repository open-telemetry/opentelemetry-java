/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import javax.annotation.Nullable;
import org.assertj.core.api.AbstractAssert;

/**
 * Assertions for the {@link StatusData} of an exported {@link SpanData}.
 *
 * @since 1.16.0
 */
public final class StatusDataAssert extends AbstractAssert<StatusDataAssert, StatusData> {

  StatusDataAssert(@Nullable StatusData statusData) {
    super(statusData, StatusDataAssert.class);
  }

  /** Asserts that the status is {@link io.opentelemetry.api.trace.StatusCode#OK}. */
  public StatusDataAssert isOk() {
    return hasCode(StatusCode.OK);
  }

  /** Asserts that the status is {@link io.opentelemetry.api.trace.StatusCode#ERROR}. */
  public StatusDataAssert isError() {
    return hasCode(StatusCode.ERROR);
  }

  /** Asserts that the status has the given {@link io.opentelemetry.api.trace.StatusCode}. */
  public StatusDataAssert hasCode(StatusCode expected) {
    isNotNull();
    assertThat(actual.getStatusCode()).isEqualTo(expected);
    return this;
  }

  /** Asserts that the status has the given description. */
  public StatusDataAssert hasDescription(String expected) {
    isNotNull();
    assertThat(actual.getDescription()).isEqualTo(expected);
    return this;
  }

  /** Asserts that the status has a description matching the given regular expression. */
  public StatusDataAssert hasDescriptionMatching(String regex) {
    isNotNull();
    assertThat(actual.getDescription()).matches(regex);
    return this;
  }
}
