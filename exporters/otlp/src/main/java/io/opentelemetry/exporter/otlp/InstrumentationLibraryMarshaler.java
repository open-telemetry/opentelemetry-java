/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedOutputStream;
import io.opentelemetry.proto.common.v1.InstrumentationLibrary;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import java.io.IOException;
import javax.annotation.Nullable;

final class InstrumentationLibraryMarshaler extends MarshalerWithSize {
  private final ByteString name;
  private final ByteString version;

  @Nullable
  static InstrumentationLibraryMarshaler create(InstrumentationLibraryInfo libraryInfo) {
    ByteString name = MarshalerUtil.toByteString(libraryInfo.getName());
    ByteString version = MarshalerUtil.toByteString(libraryInfo.getVersion());
    int size = computeSize(name, version);
    if (size == 0) {
      return null;
    }
    return new InstrumentationLibraryMarshaler(size, name, version);
  }

  private InstrumentationLibraryMarshaler(int size, ByteString name, ByteString version) {
    super(size);
    this.name = name;
    this.version = version;
  }

  @Override
  public void writeTo(CodedOutputStream output) throws IOException {
    MarshalerUtil.marshalBytes(InstrumentationLibrary.NAME_FIELD_NUMBER, name, output);
    MarshalerUtil.marshalBytes(InstrumentationLibrary.VERSION_FIELD_NUMBER, version, output);
  }

  private static int computeSize(ByteString name, ByteString version) {
    return MarshalerUtil.sizeBytes(InstrumentationLibrary.NAME_FIELD_NUMBER, name)
        + MarshalerUtil.sizeBytes(InstrumentationLibrary.VERSION_FIELD_NUMBER, version);
  }
}
