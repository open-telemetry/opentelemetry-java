/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.logs.EventBuilder;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import javax.annotation.Nullable;

/** SDK implementation of {@link Logger}. */
final class SdkLogger implements Logger {

  private final LoggerSharedState loggerSharedState;
  private final InstrumentationScopeInfo instrumentationScopeInfo;
  @Nullable private final String eventDomain;

  SdkLogger(
      LoggerSharedState loggerSharedState, InstrumentationScopeInfo instrumentationScopeInfo) {
    this(loggerSharedState, instrumentationScopeInfo, null);
  }

  SdkLogger(
      LoggerSharedState loggerSharedState,
      InstrumentationScopeInfo instrumentationScopeInfo,
      @Nullable String eventDomain) {
    this.loggerSharedState = loggerSharedState;
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.eventDomain = eventDomain;
  }

  /**
   * Return a logger identical to {@code this} ensuring the {@link #eventDomain} is equal to {@code
   * eventDomain}. If {@link #eventDomain} is not equal, creates a new instance.
   */
  SdkLogger withEventDomain(String eventDomain) {
    if (!eventDomain.equals(this.eventDomain)) {
      return new SdkLogger(loggerSharedState, instrumentationScopeInfo, eventDomain);
    }
    return this;
  }

  @Override
  public EventBuilder eventBuilder(String name) {
    if (eventDomain == null) {
      throw new IllegalStateException(
          "Cannot emit event from Logger without event domain. Please use LoggerBuilder#setEventDomain(String) when obtaining Logger.");
    }
    return new SdkLogRecordBuilder(loggerSharedState, instrumentationScopeInfo)
        .setAttribute(AttributeKey.stringKey("event.domain"), eventDomain)
        .setAttribute(AttributeKey.stringKey("event.name"), name);
  }

  @Override
  public LogRecordBuilder logRecordBuilder() {
    return new SdkLogRecordBuilder(loggerSharedState, instrumentationScopeInfo);
  }

  // VisibleForTesting
  InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return instrumentationScopeInfo;
  }
}
