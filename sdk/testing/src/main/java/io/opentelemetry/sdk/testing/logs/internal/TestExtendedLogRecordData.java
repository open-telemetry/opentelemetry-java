/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.logs.internal;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.incubator.common.ExtendedAttributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.data.internal.ExtendedLogRecordData;
import io.opentelemetry.sdk.resources.Resource;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Immutable representation of {@link LogRecordData}.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 *
 * @since 1.27.0
 */
@Immutable
@AutoValue
@AutoValue.CopyAnnotations
// Carry suppression for Body to AutoValue implementation via @AutoValue.CopyAnnotations
@SuppressWarnings("deprecation")
public abstract class TestExtendedLogRecordData implements ExtendedLogRecordData {

  /** Creates a new Builder for creating an {@link LogRecordData} instance. */
  public static Builder builder() {
    return new AutoValue_TestExtendedLogRecordData.Builder()
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
  public Body getBody() {
    Value<?> valueBody = getBodyValue();
    return valueBody == null ? Body.empty() : Body.string(valueBody.asString());
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.42.0
   */
  @Override
  @Nullable
  public abstract Value<?> getBodyValue();

  @Override
  @Nullable
  public abstract String getEventName();

  TestExtendedLogRecordData() {}

  /**
   * A {@code Builder} class for {@link TestExtendedLogRecordData}.
   *
   * <p>This class is internal and experimental. Its APIs are unstable and can change at any time.
   * Its APIs (or a version of them) may be promoted to the public stable API in the future, but no
   * guarantees are made.
   */
  @AutoValue.Builder
  public abstract static class Builder {

    abstract TestExtendedLogRecordData autoBuild();

    /** Create a new {@link LogRecordData} instance from the data in this. */
    public TestExtendedLogRecordData build() {
      return autoBuild();
    }

    /** Set the {@link Resource}. */
    public abstract Builder setResource(Resource resource);

    /** Sets the {@link InstrumentationScopeInfo}. */
    public abstract Builder setInstrumentationScopeInfo(
        InstrumentationScopeInfo instrumentationScopeInfo);

    /** Sets the event name. */
    public abstract Builder setEventName(String eventName);

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
      return setBodyValue(Value.of(body));
    }

    /**
     * Set the body.
     *
     * @deprecated Use {@link #setBodyValue(Value)}.
     */
    @Deprecated
    public Builder setBody(Body body) {
      if (body.getType() == Body.Type.STRING) {
        setBodyValue(Value.of(body.asString()));
      } else if (body.getType() == Body.Type.EMPTY) {
        setBodyValue(null);
      }
      return this;
    }

    /**
     * Set the body.
     *
     * @since 1.42.0
     */
    public abstract Builder setBodyValue(@Nullable Value<?> body);

    /** Set the attributes. */
    public Builder setAttributes(Attributes attributes) {
      return setExtendedAttributes(ExtendedAttributes.builder().putAll(attributes).build());
    }

    /** Set the total attribute count. */
    public abstract Builder setTotalAttributeCount(int totalAttributeCount);

    /** Set extended attributes. * */
    public abstract Builder setExtendedAttributes(ExtendedAttributes extendedAttributes);
  }
}
