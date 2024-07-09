/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.profiles.v1experimental.internal.Function;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

final class FunctionMarshaler extends MarshalerWithSize {

  private static final FunctionMarshaler[] EMPTY_REPEATED = new FunctionMarshaler[0];

  private final long nameIndex;
  private final long systemNameIndex;
  private final long filenameIndex;
  private final long startLine;

  static FunctionMarshaler create(FunctionData functionData) {
    FunctionMarshaler functionMarshaler =
        new FunctionMarshaler(
            functionData.getNameIndex(),
            functionData.getSystemNameIndex(),
            functionData.getFilenameIndex(),
            functionData.getStartLine());
    return functionMarshaler;
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
      long nameIndex, long systemNameIndex, long filenameIndex, long startLine) {
    super(calculateSize(nameIndex, systemNameIndex, filenameIndex, startLine));
    this.nameIndex = nameIndex;
    this.systemNameIndex = systemNameIndex;
    this.filenameIndex = filenameIndex;
    this.startLine = startLine;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeInt64(Function.NAME, nameIndex);
    output.serializeInt64(Function.SYSTEM_NAME, systemNameIndex);
    output.serializeInt64(Function.FILENAME, filenameIndex);
    output.serializeInt64(Function.START_LINE, startLine);
  }

  private static int calculateSize(
      long nameIndex, long systemNameIndex, long filenameIndex, long startLine) {
    int size = 0;
    size += MarshalerUtil.sizeInt64(Function.NAME, nameIndex);
    size += MarshalerUtil.sizeInt64(Function.SYSTEM_NAME, systemNameIndex);
    size += MarshalerUtil.sizeInt64(Function.FILENAME, filenameIndex);
    size += MarshalerUtil.sizeInt64(Function.START_LINE, startLine);
    return size;
  }
}
