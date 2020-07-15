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

package io.opentelemetry.logs;

import io.opentelemetry.common.AttributeValue;
import java.util.Map;

public class DefaultLogRecord implements LogRecord {
  private long timestamp;
  private byte[] traceId;
  private byte[] spanId;
  private int flags;
  private Severity severity;
  private String severityText;
  private String name;
  private Object body;
  private Map<String, AttributeValue> attributes;

  private DefaultLogRecord() {}

  /**
   * Clone method, used so that changes through the builder do not affect the record built.
   *
   * @param template LogRecord from which to copy
   */
  private DefaultLogRecord(DefaultLogRecord template) {
    timestamp = template.timestamp;
    traceId = template.traceId;
    spanId = template.spanId;
    flags = template.flags;
    severity = template.severity;
    severityText = template.severityText;
    name = template.name;
    body = template.body;
    attributes = template.attributes;
  }

  @Override
  public long getTimeUnixNano() {
    return timestamp;
  }

  @Override
  public byte[] getTraceId() {
    return traceId;
  }

  @Override
  public byte[] getSpanId() {
    return spanId;
  }

  @Override
  public int getFlags() {
    return flags;
  }

  @Override
  public Severity getSeverity() {
    return severity;
  }

  @Override
  public String getSeverityText() {
    return severityText;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Object getBody() {
    return body;
  }

  @Override
  public Map<String, AttributeValue> getAttributes() {
    return attributes;
  }

  public static class Builder implements LogRecord.Builder {
    DefaultLogRecord template = new DefaultLogRecord();

    @Override
    public LogRecord.Builder withUnixTimeNano(long timestamp) {
      template.timestamp = timestamp;
      return this;
    }

    @Override
    public LogRecord.Builder withTraceId(byte[] traceId) {
      template.traceId = traceId;
      return this;
    }

    @Override
    public LogRecord.Builder withSpanId(byte[] spanId) {
      template.spanId = spanId;
      return this;
    }

    @Override
    public LogRecord.Builder withFlags(int flags) {
      template.flags = flags;
      return this;
    }

    @Override
    public LogRecord.Builder withSeverity(Severity severity) {
      template.severity = severity;
      return this;
    }

    @Override
    public LogRecord.Builder withSeverityText(String severityText) {
      template.severityText = severityText;
      return this;
    }

    @Override
    public LogRecord.Builder withName(String name) {
      template.name = name;
      return this;
    }

    @Override
    public LogRecord.Builder withBody(Object body) {
      template.body = body;
      return this;
    }

    @Override
    public LogRecord.Builder withAttributes(Map<String, AttributeValue> attributes) {
      template.attributes = attributes;
      return this;
    }

    @Override
    public LogRecord build() {
      return new DefaultLogRecord(template);
    }
  }
}
