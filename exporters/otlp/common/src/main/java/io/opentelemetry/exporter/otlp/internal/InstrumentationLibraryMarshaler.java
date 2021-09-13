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

  private final byte[] serializedBinary;
  private final String serializedJson;

  static InstrumentationLibraryMarshaler create(InstrumentationLibraryInfo libraryInfo) {
    InstrumentationLibraryMarshaler cached = LIBRARY_MARSHALER_CACHE.get(libraryInfo);
    if (cached == null) {
      // Since WeakConcurrentMap doesn't support computeIfAbsent, we may end up doing the conversion
      // a few times until the cache gets filled which is fine.
      byte[] name = MarshalerUtil.toBytes(libraryInfo.getName());
      byte[] version = MarshalerUtil.toBytes(libraryInfo.getVersion());

      RealInstrumentationLibraryMarshaler realMarshaler =
          new RealInstrumentationLibraryMarshaler(name, version);

      ByteArrayOutputStream binaryBos =
          new ByteArrayOutputStream(realMarshaler.getBinarySerializedSize());

      try {
        realMarshaler.writeBinaryTo(binaryBos);
      } catch (IOException e) {
        throw new UncheckedIOException(
            "Serialization error, this is likely a bug in OpenTelemetry.", e);
      }

      String json = MarshalerUtil.preserializeJsonFields(realMarshaler);

      cached = new InstrumentationLibraryMarshaler(binaryBos.toByteArray(), json);
      LIBRARY_MARSHALER_CACHE.put(libraryInfo, cached);
    }
    return cached;
  }

  private InstrumentationLibraryMarshaler(byte[] binary, String json) {
    super(binary.length);
    serializedBinary = binary;
    serializedJson = json;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.writeSerializedMessage(serializedBinary, serializedJson);
  }

  private static final class RealInstrumentationLibraryMarshaler extends MarshalerWithSize {

    private final byte[] name;
    private final byte[] version;

    RealInstrumentationLibraryMarshaler(byte[] name, byte[] version) {
      super(computeSize(name, version));
      this.name = name;
      this.version = version;
    }

    @Override
    void writeTo(Serializer output) throws IOException {
      output.serializeString(InstrumentationLibrary.NAME, name);
      output.serializeString(InstrumentationLibrary.VERSION, version);
    }

    private static int computeSize(byte[] name, byte[] version) {
      return MarshalerUtil.sizeBytes(InstrumentationLibrary.NAME, name)
          + MarshalerUtil.sizeBytes(InstrumentationLibrary.VERSION, version);
    }
  }
}
