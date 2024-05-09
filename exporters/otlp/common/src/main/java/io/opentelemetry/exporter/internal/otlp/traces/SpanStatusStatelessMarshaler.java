/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.traces;

import static io.opentelemetry.exporter.internal.otlp.traces.SpanStatusMarshaler.toProtoSpanStatus;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.ProtoEnumInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtil;
import io.opentelemetry.proto.trace.v1.internal.Status;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.io.IOException;

/** See {@link SpanStatusMarshaler}. */
final class SpanStatusStatelessMarshaler implements StatelessMarshaler<StatusData> {
  static final SpanStatusStatelessMarshaler INSTANCE = new SpanStatusStatelessMarshaler();

  private SpanStatusStatelessMarshaler() {}

  @Override
  public void writeTo(Serializer output, StatusData status, MarshalerContext context)
      throws IOException {
    ProtoEnumInfo protoStatusCode = toProtoSpanStatus(status);

    output.serializeStringWithContext(Status.MESSAGE, status.getDescription(), context);
    output.serializeEnum(Status.CODE, protoStatusCode);
  }

  @Override
  public int getBinarySerializedSize(StatusData status, MarshalerContext context) {
    ProtoEnumInfo protoStatusCode = toProtoSpanStatus(status);

    int size = 0;
    size +=
        StatelessMarshalerUtil.sizeStringWithContext(
            Status.MESSAGE, status.getDescription(), context);
    size += MarshalerUtil.sizeEnum(Status.CODE, protoStatusCode);

    return size;
  }
}
