/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.StringAssert;

/** Assertions for the {@link StatusData} of an exported {@link SpanData}. */
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

  /** Asserts that the status has a description matching the given pattern. */
  public StatusDataAssert hasDescriptionMatching(Pattern pattern) {
    isNotNull();
    assertThat(actual.getDescription()).matches(pattern);
    return this;
  }

  /** Asserts that the status has a description that satisfies the given condition. */
  public StatusDataAssert hasDescriptionSatisfying(Consumer<StringAssert> condition) {
    isNotNull();
    StringAssert stringAssert = new StringAssert(actual.getDescription());
    condition.accept(stringAssert);
    return this;
  }
}
