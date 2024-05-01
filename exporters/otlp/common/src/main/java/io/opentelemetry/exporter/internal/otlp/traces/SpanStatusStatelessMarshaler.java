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
import io.opentelemetry.proto.trace.v1.internal.Status;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.io.IOException;

/** See {@link SpanStatusMarshaler}. */
final class SpanStatusStatelessMarshaler implements StatelessMarshaler<StatusData> {
  static final SpanStatusStatelessMarshaler INSTANCE = new SpanStatusStatelessMarshaler();

  @Override
  public void writeTo(Serializer output, StatusData status, MarshalerContext context)
      throws IOException {
    ProtoEnumInfo protoStatusCode = toProtoSpanStatus(status);
    byte[] descriptionUtf8 = context.getData(byte[].class);

    output.serializeString(Status.MESSAGE, descriptionUtf8);
    output.serializeEnum(Status.CODE, protoStatusCode);
  }

  @Override
  public int getBinarySerializedSize(StatusData status, MarshalerContext context) {
    ProtoEnumInfo protoStatusCode = toProtoSpanStatus(status);
    byte[] descriptionUtf8 = MarshalerUtil.toBytes(status.getDescription());
    context.addData(descriptionUtf8);

    int size = 0;
    size += MarshalerUtil.sizeBytes(Status.MESSAGE, descriptionUtf8);
    size += MarshalerUtil.sizeEnum(Status.CODE, protoStatusCode);

    return size;
  }
}
