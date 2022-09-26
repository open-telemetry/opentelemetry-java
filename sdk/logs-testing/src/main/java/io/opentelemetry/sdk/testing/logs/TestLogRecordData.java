/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.logs;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.Immutable;

/** Immutable representation of {@link LogRecordData}. */
@Immutable
@AutoValue
public abstract class TestLogRecordData implements LogRecordData {

  /** Creates a new Builder for creating an {@link LogRecordData} instance. */
  public static Builder builder() {
    return new AutoValue_TestLogRecordData.Builder()
        .setResource(Resource.empty())
        .setInstrumentationScopeInfo(InstrumentationScopeInfo.empty())
        .setEpoch(0, TimeUnit.NANOSECONDS)
        .setSpanContext(SpanContext.getInvalid())
        .setSeverity(Severity.UNDEFINED_SEVERITY_NUMBER)
        .setBody("")
        .setAttributes(Attributes.empty())
        .setTotalAttributeCount(0);
  }

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

    /** Set the epoch timestamp to the {@code instant}. */
    public Builder setEpoch(Instant instant) {
      return setEpochNanos(TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano());
    }

    /** Set the epoch timestamp. */
    public Builder setEpoch(long timestamp, TimeUnit unit) {
      return setEpochNanos(unit.toNanos(timestamp));
    }

    /** Set the epoch timestamp in nanos. */
    abstract Builder setEpochNanos(long epochNanos);

    /** Set the span context. */
    public abstract Builder setSpanContext(SpanContext spanContext);

    /** Set the severity. */
    public abstract Builder setSeverity(Severity severity);

    /** Set the severity text. */
    public abstract Builder setSeverityText(String severityText);

    /** Set the body string. */
    public Builder setBody(String body) {
      return setBody(Body.string(body));
    }

    /** Set the body. */
    abstract Builder setBody(Body body);

    /** Set the attributes. */
    public abstract Builder setAttributes(Attributes attributes);

    /** Set the total attribute count. */
    public abstract Builder setTotalAttributeCount(int totalAttributeCount);
  }
}
