/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.logs;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.AnyValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Immutable representation of {@link LogRecordData}.
 *
 * @since 1.27.0
 */
@Immutable
@AutoValue
@AutoValue.CopyAnnotations
@SuppressWarnings("deprecation") // Carry suppression for Body to AutoValue implementation via
// @AutoValue.CopyAnnotations
public abstract class TestLogRecordData implements LogRecordData {

  /** Creates a new Builder for creating an {@link LogRecordData} instance. */
  public static Builder builder() {
    return new AutoValue_TestLogRecordData.Builder()
        .setResource(Resource.empty())
        .setInstrumentationScopeInfo(InstrumentationScopeInfo.empty())
        .setTimestamp(0, TimeUnit.NANOSECONDS)
        .setObservedTimestamp(0, TimeUnit.NANOSECONDS)
        .setSpanContext(SpanContext.getInvalid())
        .setSeverity(Severity.UNDEFINED_SEVERITY_NUMBER)
        .setAttributes(Attributes.empty())
        .setTotalAttributeCount(0);
  }

  @Deprecated
  public io.opentelemetry.sdk.logs.data.Body getBody() {
    AnyValue<?> anyValueBody = getAnyValueBody();
    return anyValueBody == null
        ? io.opentelemetry.sdk.logs.data.Body.empty()
        : io.opentelemetry.sdk.logs.data.Body.string(anyValueBody.asString());
  }

  @Override
  @Nullable
  public abstract AnyValue<?> getAnyValueBody();

  TestLogRecordData() {}

  /** A {@code Builder} class for {@link TestLogRecordData}. */
  @AutoValue.Builder
  public abstract static class Builder {

    abstract TestLogRecordData autoBuild();

    /** Create a new {@link LogRecordData} instance from the data in this. */
    public TestLogRecordData build() {
      return autoBuild();
    }

    /** Set the {@link Resource}. */
    public abstract Builder setResource(Resource resource);

    /** Sets the {@link InstrumentationScopeInfo}. */
    public abstract Builder setInstrumentationScopeInfo(
        InstrumentationScopeInfo instrumentationScopeInfo);

    /**
     * Set the epoch {@code timestamp}, using the instant.
     *
     * <p>The {@code timestamp} is the time at which the log record occurred.
     */
    public Builder setTimestamp(Instant instant) {
      return setTimestampEpochNanos(
          TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano());
    }

    /**
     * Set the epoch {@code timestamp}, using the timestamp and unit.
     *
     * <p>The {@code timestamp} is the time at which the log record occurred.
     */
    public Builder setTimestamp(long timestamp, TimeUnit unit) {
      return setTimestampEpochNanos(unit.toNanos(timestamp));
    }

    /**
     * Set the epoch {@code timestamp}.
     *
     * <p>The {@code timestamp} is the time at which the log record occurred.
     */
    abstract Builder setTimestampEpochNanos(long epochNanos);

    /**
     * Set the {@code observedTimestamp}, using the instant.
     *
     * <p>The {@code observedTimestamp} is the time at which the log record was observed.
     */
    public Builder setObservedTimestamp(Instant instant) {
      return setObservedTimestampEpochNanos(
          TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano());
    }

    /**
     * Set the epoch {@code observedTimestamp}, using the timestamp and unit.
     *
     * <p>The {@code observedTimestamp} is the time at which the log record was observed.
     */
    public Builder setObservedTimestamp(long timestamp, TimeUnit unit) {
      return setObservedTimestampEpochNanos(unit.toNanos(timestamp));
    }

    /**
     * Set the epoch {@code observedTimestamp}.
     *
     * <p>The {@code observedTimestamp} is the time at which the log record was observed.
     */
    abstract Builder setObservedTimestampEpochNanos(long epochNanos);

    /** Set the span context. */
    public abstract Builder setSpanContext(SpanContext spanContext);

    /** Set the severity. */
    public abstract Builder setSeverity(Severity severity);

    /** Set the severity text. */
    public abstract Builder setSeverityText(String severityText);

    /** Set the body string. */
    public Builder setBody(String body) {
      return setAnyValueBody(AnyValue.of(body));
    }

    /**
     * Set the body.
     *
     * @deprecated Use {@link #setAnyValueBody(AnyValue)}.
     */
    @Deprecated
    public Builder setBody(io.opentelemetry.sdk.logs.data.Body body) {
      if (body.getType() == io.opentelemetry.sdk.logs.data.Body.Type.STRING) {
        setAnyValueBody(AnyValue.of(body.asString()));
      }
      return this;
    }

    /** Set the body. */
    public abstract Builder setAnyValueBody(AnyValue<?> body);

    /** Set the attributes. */
    public abstract Builder setAttributes(Attributes attributes);

    /** Set the total attribute count. */
    public abstract Builder setTotalAttributeCount(int totalAttributeCount);
  }
}
