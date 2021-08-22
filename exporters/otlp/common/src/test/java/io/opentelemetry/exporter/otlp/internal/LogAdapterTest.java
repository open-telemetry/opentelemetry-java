package io.opentelemetry.exporter.otlp.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.sdk.logging.data.LogRecord;
import org.junit.jupiter.api.Test;

public class LogAdapterTest {
  private static final byte[] TRACE_ID_BYTES =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4};
  private static final String TRACE_ID = TraceId.fromBytes(TRACE_ID_BYTES);
  private static final byte[] SPAN_ID_BYTES = new byte[] {0, 0, 0, 0, 4, 3, 2, 1};
  private static final String SPAN_ID = SpanId.fromBytes(SPAN_ID_BYTES);
  private static final String NAME = "GET /api/endpoint";
  private static final String BODY = "Hello world from this log...";

  @Test
  void toProtoResourceLogs() {
//    LogRecord logRecord = LogAdapter.toProtoLogRecord(
//
//    )
  }
//
  @Test
  void toProtoLogRecord() {
    io.opentelemetry.proto.logs.v1.LogRecord logRecord =
        LogAdapter.toProtoLogRecord(
            LogRecord.builder()
                .setName(NAME)
                .setBody(BODY)
                .setSeverity(LogRecord.Severity.INFO)
                .setSeverityText("INFO")
                .setTraceId(TRACE_ID)
                .setSpanId(SPAN_ID)
                .setAttributes(Attributes.of(AttributeKey.booleanKey("key"), true))
                .setUnixTimeNano(12345)
                .build());

    assertThat(logRecord.getTraceId().toByteArray()).isEqualTo(TRACE_ID_BYTES);
    assertThat(logRecord.getSpanId().toByteArray()).isEqualTo(SPAN_ID_BYTES);
    assertThat(logRecord.getName()).isEqualTo(NAME);
    assertThat(logRecord.getBody().toString()).isEqualTo(BODY);
    assertThat(logRecord.getAttributesList())
        .containsExactly(
            KeyValue.newBuilder()
                .setKey("key")
                .setValue(AnyValue.newBuilder().setBoolValue(true).build())
                .build());
    assertThat(logRecord.getTimeUnixNano()).isEqualTo(12345);
  }

}
