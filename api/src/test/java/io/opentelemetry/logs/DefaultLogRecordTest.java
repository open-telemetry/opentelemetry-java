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

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.common.AnyValue;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.logs.LogRecord.Severity;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DefaultLogRecordTest {

  @Test
  public void testDefaultLogRecord() {
    long testTimestamp = 123L;
    byte[] testTraceId = {'a', 'b', 'c'};
    byte[] testSpanId = {'z', 'y', 'x'};
    int testFlags = 1;
    Severity testSeverity = Severity.ERROR2;
    String testSeverityText = "severityText";
    String testName = "an event name";
    AnyValue testBody = AnyValue.stringAnyValue("A test body");
    Map<String, AttributeValue> testAttributes = new HashMap<>();
    testAttributes.put("one", AttributeValue.stringAttributeValue("test1"));
    testAttributes.put("two", AttributeValue.longAttributeValue(42L));

    LogRecord.Builder builder =
        new DefaultLogRecord.Builder()
            .withUnixTimeNano(testTimestamp)
            .withTraceId(testTraceId)
            .withSpanId(testSpanId)
            .withFlags(testFlags)
            .withSeverity(testSeverity)
            .withSeverityText(testSeverityText)
            .withName(testName)
            .withAttributes(testAttributes)
            .withBody(testBody);
    LogRecord record = builder.build();

    assertThat(record.getTimeUnixNano()).isEqualTo(testTimestamp);
    assertThat(record.getTraceId()).isEqualTo(testTraceId);
    assertThat(record.getSpanId()).isEqualTo(testSpanId);
    assertThat(record.getFlags()).isEqualTo(testFlags);
    assertThat(record.getSeverity()).isEqualTo(testSeverity);
    assertThat(record.getSeverityText()).isEqualTo(testSeverityText);
    assertThat(record.getName()).isEqualTo(testName);
    assertThat(record.getAttributes()).isEqualTo(testAttributes);
    assertThat(record.getBody()).isEqualTo(testBody);

    LogRecord secondRecord = builder.withUnixTimeNano(456L).build();
    assertThat(secondRecord.getTimeUnixNano()).isEqualTo(456L);
    assertThat(record.getTimeUnixNano()).isNotEqualTo(456L);
  }
}
