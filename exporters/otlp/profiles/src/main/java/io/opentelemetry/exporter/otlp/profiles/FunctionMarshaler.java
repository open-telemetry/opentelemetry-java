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

  private final int nameStrindex;
  private final int systemNameStrindex;
  private final int filenameStrindex;
  private final long startLine;

  static FunctionMarshaler create(FunctionData functionData) {
    return new FunctionMarshaler(
        functionData.getNameStrindex(),
        functionData.getSystemNameStrindex(),
        functionData.getFilenameStrindex(),
        functionData.getStartLine());
  }

  static FunctionMarshaler[] createRepeated(List<FunctionData> items) {
    if (items.isEmpty()) {
      return EMPTY_REPEATED;
    }

    FunctionMarshaler[] functionMarshalers = new FunctionMarshaler[items.size()];
    items.forEach(
        item ->
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
      int nameStrindex, int systemNameStrindex, int filenameStrindex, long startLine) {
    super(calculateSize(nameStrindex, systemNameStrindex, filenameStrindex, startLine));
    this.nameStrindex = nameStrindex;
    this.systemNameStrindex = systemNameStrindex;
    this.filenameStrindex = filenameStrindex;
    this.startLine = startLine;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeInt32(Function.NAME_STRINDEX, nameStrindex);
    output.serializeInt32(Function.SYSTEM_NAME_STRINDEX, systemNameStrindex);
    output.serializeInt32(Function.FILENAME_STRINDEX, filenameStrindex);
    output.serializeInt64(Function.START_LINE, startLine);
  }

  private static int calculateSize(
      int nameStrindex, int systemNameStrindex, int filenameStrindex, long startLine) {
    int size = 0;
    size += MarshalerUtil.sizeInt32(Function.NAME_STRINDEX, nameStrindex);
    size += MarshalerUtil.sizeInt32(Function.SYSTEM_NAME_STRINDEX, systemNameStrindex);
    size += MarshalerUtil.sizeInt32(Function.FILENAME_STRINDEX, filenameStrindex);
    size += MarshalerUtil.sizeInt64(Function.START_LINE, startLine);
    return size;
  }
}
