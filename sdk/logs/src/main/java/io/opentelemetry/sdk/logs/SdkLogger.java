/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.internal.ValidationUtil;
import io.opentelemetry.api.logs.EventBuilder;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import java.util.logging.Level;
import javax.annotation.Nullable;

/** SDK implementation of {@link Logger}. */
final class SdkLogger implements Logger {

  // Obtain a noop logger with the domain set so that we can obtain noop EventBuilder without
  // generating additional warning logs
  private static final Logger NOOP_LOGGER_WITH_DOMAIN =
      LoggerProvider.noop().loggerBuilder("unused").setEventDomain("unused").build();

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
  public EventBuilder eventBuilder(String eventName) {
    if (eventDomain == null) {
      ValidationUtil.log(
          "Cannot emit event from Logger without event domain. Please use LoggerBuilder#setEventDomain(String) when obtaining Logger.",
          Level.WARNING);
      return NOOP_LOGGER_WITH_DOMAIN.eventBuilder(eventName);
    }
    return new SdkLogRecordBuilder(loggerSharedState, instrumentationScopeInfo)
        .setAttribute(AttributeKey.stringKey("event.domain"), eventDomain)
        .setAttribute(AttributeKey.stringKey("event.name"), eventName);
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
