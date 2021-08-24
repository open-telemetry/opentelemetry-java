/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.context.internal.shaded.WeakConcurrentMap;
import io.opentelemetry.proto.common.v1.internal.InstrumentationLibrary;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

final class InstrumentationLibraryMarshaler extends MarshalerWithSize {

  private static final WeakConcurrentMap<
          InstrumentationLibraryInfo, InstrumentationLibraryMarshaler>
      LIBRARY_MARSHALER_CACHE = new WeakConcurrentMap.WithInlinedExpunction<>();

  private final byte[] serializedInfo;

  static InstrumentationLibraryMarshaler create(InstrumentationLibraryInfo libraryInfo) {
    InstrumentationLibraryMarshaler cached = LIBRARY_MARSHALER_CACHE.get(libraryInfo);
    if (cached == null) {
      // Since WeakConcurrentMap doesn't support computeIfAbsent, we may end up doing the conversion
      // a few times until the cache gets filled which is fine.
      byte[] name = MarshalerUtil.toBytes(libraryInfo.getName());
      byte[] version = MarshalerUtil.toBytes(libraryInfo.getVersion());
      cached = new InstrumentationLibraryMarshaler(name, version);
      LIBRARY_MARSHALER_CACHE.put(libraryInfo, cached);
    }
    return cached;
  }

  private InstrumentationLibraryMarshaler(byte[] name, byte[] version) {
    super(computeSize(name, version));
    ByteArrayOutputStream bos = new ByteArrayOutputStream(getSerializedSize());
    CodedOutputStream output =
        CodedOutputStream.newInstance(
            bos, CodedOutputStream.computePreferredBufferSize(getSerializedSize()));
    try {
      MarshalerUtil.marshalBytes(InstrumentationLibrary.NAME_FIELD_NUMBER, name, output);
      MarshalerUtil.marshalBytes(InstrumentationLibrary.VERSION_FIELD_NUMBER, version, output);
      output.flush();
    } catch (IOException e) {
      // Presized so can't happen (we would have already thrown OutOfMemoryError)
      throw new UncheckedIOException(e);
    }
    serializedInfo = bos.toByteArray();
  }

  @Override
  public void writeTo(CodedOutputStream output) throws IOException {
    output.writeRawBytes(serializedInfo);
  }

  private static int computeSize(byte[] name, byte[] version) {
    return MarshalerUtil.sizeBytes(InstrumentationLibrary.NAME_FIELD_NUMBER, name)
        + MarshalerUtil.sizeBytes(InstrumentationLibrary.VERSION_FIELD_NUMBER, version);
  }
}
