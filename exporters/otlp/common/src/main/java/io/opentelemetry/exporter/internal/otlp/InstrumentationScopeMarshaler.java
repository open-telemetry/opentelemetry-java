/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.context.internal.shaded.WeakConcurrentMap;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.common.v1.internal.InstrumentationScope;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * A Marshaler of {@link InstrumentationScopeInfo}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class InstrumentationScopeMarshaler extends MarshalerWithSize {

  private static final WeakConcurrentMap<InstrumentationScopeInfo, InstrumentationScopeMarshaler>
      SCOPE_MARSHALER_CACHE = new WeakConcurrentMap.WithInlinedExpunction<>();

  private final byte[] serializedBinary;
  private final String serializedJson;

  /** Returns a Marshaler for InstrumentationScopeInfo. */
  public static InstrumentationScopeMarshaler create(InstrumentationScopeInfo scopeInfo) {
    InstrumentationScopeMarshaler cached = SCOPE_MARSHALER_CACHE.get(scopeInfo);
    if (cached == null) {
      // Since WeakConcurrentMap doesn't support computeIfAbsent, we may end up doing the conversion
      // a few times until the cache gets filled which is fine.
      byte[] name = MarshalerUtil.toBytes(scopeInfo.getName());
      byte[] version = MarshalerUtil.toBytes(scopeInfo.getVersion());
      KeyValueMarshaler[] attributes = KeyValueMarshaler.createRepeated(scopeInfo.getAttributes());

      RealInstrumentationScopeMarshaler realMarshaler =
          new RealInstrumentationScopeMarshaler(name, version, attributes);

      ByteArrayOutputStream binaryBos =
          new ByteArrayOutputStream(realMarshaler.getBinarySerializedSize());

      try {
        realMarshaler.writeBinaryTo(binaryBos);
      } catch (IOException e) {
        throw new UncheckedIOException(
            "Serialization error, this is likely a bug in OpenTelemetry.", e);
      }

      String json = MarshalerUtil.preserializeJsonFields(realMarshaler);

      cached = new InstrumentationScopeMarshaler(binaryBos.toByteArray(), json);
      SCOPE_MARSHALER_CACHE.put(scopeInfo, cached);
    }
    return cached;
  }

  private InstrumentationScopeMarshaler(byte[] binary, String json) {
    super(binary.length);
    serializedBinary = binary;
    serializedJson = json;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.writeSerializedMessage(serializedBinary, serializedJson);
  }

  private static final class RealInstrumentationScopeMarshaler extends MarshalerWithSize {

    private final byte[] name;
    private final byte[] version;
    private final KeyValueMarshaler[] attributes;

    RealInstrumentationScopeMarshaler(byte[] name, byte[] version, KeyValueMarshaler[] attributes) {
      super(computeSize(name, version, attributes));
      this.name = name;
      this.version = version;
      this.attributes = attributes;
    }

    @Override
    protected void writeTo(Serializer output) throws IOException {
      output.serializeString(InstrumentationScope.NAME, name);
      output.serializeString(InstrumentationScope.VERSION, version);
      output.serializeRepeatedMessage(InstrumentationScope.ATTRIBUTES, attributes);
    }

    private static int computeSize(byte[] name, byte[] version, KeyValueMarshaler[] attributes) {
      return MarshalerUtil.sizeBytes(InstrumentationScope.NAME, name)
          + MarshalerUtil.sizeBytes(InstrumentationScope.VERSION, version)
          + MarshalerUtil.sizeRepeatedMessage(InstrumentationScope.ATTRIBUTES, attributes);
    }
  }
}
