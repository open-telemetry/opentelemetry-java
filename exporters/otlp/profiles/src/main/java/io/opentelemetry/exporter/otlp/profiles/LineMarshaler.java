/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.profiles.v1development.internal.Line;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

final class LineMarshaler extends MarshalerWithSize {

  private static final LineMarshaler[] EMPTY_REPEATED = new LineMarshaler[0];

  private final int functionIndex;
  private final long line;
  private final long column;

  static LineMarshaler create(LineData lineData) {
    return new LineMarshaler(lineData.getFunctionIndex(), lineData.getLine(), lineData.getColumn());
  }

  static LineMarshaler[] createRepeated(List<LineData> items) {
    if (items.isEmpty()) {
      return EMPTY_REPEATED;
    }

    LineMarshaler[] lineMarshalers = new LineMarshaler[items.size()];
    items.forEach(
        new Consumer<LineData>() {
          int index = 0;

          @Override
          public void accept(LineData lineData) {
            lineMarshalers[index++] = LineMarshaler.create(lineData);
          }
        });
    return lineMarshalers;
  }

  private LineMarshaler(int functionIndex, long line, long column) {
    super(calculateSize(functionIndex, line, column));
    this.functionIndex = functionIndex;
    this.line = line;
    this.column = column;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeInt32(Line.FUNCTION_INDEX, functionIndex);
    output.serializeInt64(Line.LINE, line);
    output.serializeInt64(Line.COLUMN, column);
  }

  private static int calculateSize(int functionIndex, long line, long column) {
    int size = 0;
    size += MarshalerUtil.sizeInt32(Line.FUNCTION_INDEX, functionIndex);
    size += MarshalerUtil.sizeInt64(Line.LINE, line);
    size += MarshalerUtil.sizeInt64(Line.COLUMN, column);
    return size;
  }
}
