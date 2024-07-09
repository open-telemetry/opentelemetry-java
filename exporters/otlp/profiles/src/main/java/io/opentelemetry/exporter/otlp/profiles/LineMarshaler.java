/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.profiles.v1experimental.internal.Line;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

final class LineMarshaler extends MarshalerWithSize {

  private static final LineMarshaler[] EMPTY_REPEATED = new LineMarshaler[0];

  private final long functionIndex;
  private final long line;
  private final long column;

  static LineMarshaler create(LineData lineData) {
    LineMarshaler lineMarshaler =
        new LineMarshaler(lineData.getFunctionIndex(), lineData.getLine(), lineData.getColumn());
    return lineMarshaler;
  }

  static LineMarshaler[] createRepeated(List<LineData> items) {
    if (items.isEmpty()) {
      return EMPTY_REPEATED;
    }

    LineMarshaler[] lineMarshalers = new LineMarshaler[items.size()];
    items.forEach(
        item ->
            new Consumer<LineData>() {
              int index = 0;

              @Override
              public void accept(LineData lineData) {
                lineMarshalers[index++] = LineMarshaler.create(lineData);
              }
            });
    return lineMarshalers;
  }

  private LineMarshaler(long functionIndex, long line, long column) {
    super(calculateSize(functionIndex, line, column));
    this.functionIndex = functionIndex;
    this.line = line;
    this.column = column;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeUInt64(Line.FUNCTION_INDEX, functionIndex);
    output.serializeInt64(Line.LINE, line);
    output.serializeInt64(Line.COLUMN, column);
  }

  private static int calculateSize(long functionIndex, long line, long column) {
    int size = 0;
    size += MarshalerUtil.sizeUInt64(Line.FUNCTION_INDEX, functionIndex);
    size += MarshalerUtil.sizeInt64(Line.LINE, line);
    size += MarshalerUtil.sizeInt64(Line.COLUMN, column);
    return size;
  }
}
