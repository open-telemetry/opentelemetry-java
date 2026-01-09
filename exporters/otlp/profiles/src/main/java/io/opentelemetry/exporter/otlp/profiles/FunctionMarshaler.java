/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.profiles.v1development.internal.Function;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

final class FunctionMarshaler extends MarshalerWithSize {

  private static final FunctionMarshaler[] EMPTY_REPEATED = new FunctionMarshaler[0];

  private final int nameStringIndex;
  private final int systemNameStringIndex;
  private final int filenameStringIndex;
  private final long startLine;

  static FunctionMarshaler create(FunctionData functionData) {
    return new FunctionMarshaler(
        functionData.getNameStringIndex(),
        functionData.getSystemNameStringIndex(),
        functionData.getFilenameStringIndex(),
        functionData.getStartLine());
  }

  static FunctionMarshaler[] createRepeated(List<FunctionData> items) {
    if (items.isEmpty()) {
      return EMPTY_REPEATED;
    }

    FunctionMarshaler[] functionMarshalers = new FunctionMarshaler[items.size()];
    items.forEach(
        new Consumer<FunctionData>() {
          int index = 0;

          @Override
          public void accept(FunctionData functionData) {
            functionMarshalers[index++] = FunctionMarshaler.create(functionData);
          }
        });
    return functionMarshalers;
  }

  private FunctionMarshaler(
      int nameStringIndex, int systemNameStringIndex, int filenameStringIndex, long startLine) {
    super(calculateSize(nameStringIndex, systemNameStringIndex, filenameStringIndex, startLine));
    this.nameStringIndex = nameStringIndex;
    this.systemNameStringIndex = systemNameStringIndex;
    this.filenameStringIndex = filenameStringIndex;
    this.startLine = startLine;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeInt32(Function.NAME_STRINDEX, nameStringIndex);
    output.serializeInt32(Function.SYSTEM_NAME_STRINDEX, systemNameStringIndex);
    output.serializeInt32(Function.FILENAME_STRINDEX, filenameStringIndex);
    output.serializeInt64(Function.START_LINE, startLine);
  }

  private static int calculateSize(
      int nameStringIndex, int systemNameStringIndex, int filenameStringIndex, long startLine) {
    int size = 0;
    size += MarshalerUtil.sizeInt32(Function.NAME_STRINDEX, nameStringIndex);
    size += MarshalerUtil.sizeInt32(Function.SYSTEM_NAME_STRINDEX, systemNameStringIndex);
    size += MarshalerUtil.sizeInt32(Function.FILENAME_STRINDEX, filenameStringIndex);
    size += MarshalerUtil.sizeInt64(Function.START_LINE, startLine);
    return size;
  }
}
