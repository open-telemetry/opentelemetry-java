/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.logging.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.common.AttributeValue;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/**
 * A LogRecord is an implementation of the <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/logs/data-model.md">
 * OpenTelemetry logging model</a>.
 */
@AutoValue
public abstract class LogRecord {
  abstract long getTimeUnixNano();

  abstract String getTraceId();

  abstract String getSpanId();

  abstract int getFlags();

  abstract Severity getSeverity();

  @Nullable
  abstract String getSeverityText();

  @Nullable
  abstract String getName();

  abstract AnyValue getBody();

  abstract Map<String, AttributeValue> getAttributes();

  public enum Severity {
    UNDEFINED_SEVERITY_NUMBER(0),
    TRACE(1),
    TRACE2(2),
    TRACE3(3),
    TRACE4(4),
    DEBUG(5),
    DEBUG2(6),
    DEBUG3(7),
    DEBUG4(8),
    INFO(9),
    INFO2(10),
    INFO3(11),
    INFO4(12),
    WARN(13),
    WARN2(14),
    WARN3(15),
    WARN4(16),
    ERROR(17),
    ERROR2(18),
    ERROR3(19),
    ERROR4(20),
    FATAL(21),
    FATAL2(22),
    FATAL3(23),
    FATAL4(24),
    ;

    private final int severityNumber;

    Severity(int severityNumber) {
      this.severityNumber = severityNumber;
    }

    public int getSeverityNumber() {
      return severityNumber;
    }
  }

  public static class Builder {
    private long timeUnixNano;
    private String traceId = "";
    private String spanId = "";
    private int flags;
    private Severity severity = Severity.UNDEFINED_SEVERITY_NUMBER;
    private String severityText;
    private String name;
    private AnyValue body = AnyValue.stringAnyValue("");
    private final Map<String, AttributeValue> attributes = new HashMap<>();

    public Builder setUnixTimeNano(long timestamp) {
      this.timeUnixNano = timestamp;
      return this;
    }

    public Builder setUnixTimeMillis(long timestamp) {
      return setUnixTimeNano(TimeUnit.MILLISECONDS.toNanos(timestamp));
    }

    public Builder setTraceId(String traceId) {
      this.traceId = traceId;
      return this;
    }

    public Builder setSpanId(String spanId) {
      this.spanId = spanId;
      return this;
    }

    public Builder setFlags(int flags) {
      this.flags = flags;
      return this;
    }

    public Builder setSeverity(Severity severity) {
      this.severity = severity;
      return this;
    }

    public Builder setSeverityText(String severityText) {
      this.severityText = severityText;
      return this;
    }

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Builder setBody(AnyValue body) {
      this.body = body;
      return this;
    }

    public Builder setBody(String body) {
      this.body = AnyValue.stringAnyValue(body);
      return this;
    }

    public Builder setAttributes(Map<String, AttributeValue> attributes) {
      this.attributes.putAll(attributes);
      return this;
    }

    /**
     * Build a LogRecord instance.
     *
     * @return value object being built
     */
    public LogRecord build() {
      if (timeUnixNano == 0) {
        timeUnixNano = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
      }
      return new AutoValue_LogRecord(
          timeUnixNano, traceId, spanId, flags, severity, severityText, name, body, attributes);
    }
  }
}
