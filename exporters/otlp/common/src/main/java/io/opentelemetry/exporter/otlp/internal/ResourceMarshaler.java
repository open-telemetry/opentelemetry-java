/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.context.internal.shaded.WeakConcurrentMap;
import io.opentelemetry.proto.resource.v1.internal.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

final class ResourceMarshaler extends MarshalerWithSize {

  private static final WeakConcurrentMap<io.opentelemetry.sdk.resources.Resource, ResourceMarshaler>
      RESOURCE_MARSHALER_CACHE = new WeakConcurrentMap.WithInlinedExpunction<>();

  private final byte[] serializedBinary;
  private final String serializedJson;

  static ResourceMarshaler create(io.opentelemetry.sdk.resources.Resource resource) {
    ResourceMarshaler cached = RESOURCE_MARSHALER_CACHE.get(resource);
    if (cached == null) {
      // Since WeakConcurrentMap doesn't support computeIfAbsent, we may end up doing the conversion
      // a few times until the cache gets filled which is fine.

      RealResourceMarshaler realMarshaler =
          new RealResourceMarshaler(KeyValueMarshaler.createRepeated(resource.getAttributes()));

      ByteArrayOutputStream binaryBos =
          new ByteArrayOutputStream(realMarshaler.getBinarySerializedSize());

      try {
        realMarshaler.writeBinaryTo(binaryBos);
      } catch (IOException e) {
        throw new UncheckedIOException(
            "Serialization error, this is likely a bug in OpenTelemetry.", e);
      }

      String json = MarshalerUtil.preserializeJsonFields(realMarshaler);

      cached = new ResourceMarshaler(binaryBos.toByteArray(), json);
      RESOURCE_MARSHALER_CACHE.put(resource, cached);
    }
    return cached;
  }

  private ResourceMarshaler(byte[] binary, String json) {
    super(binary.length);
    serializedBinary = binary;
    serializedJson = json;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.writeSerializedMessage(serializedBinary, serializedJson);
  }

  private static final class RealResourceMarshaler extends MarshalerWithSize {
    private final KeyValueMarshaler[] attributes;

    private RealResourceMarshaler(KeyValueMarshaler[] attributes) {
      super(calculateSize(attributes));
      this.attributes = attributes;
    }

    @Override
    void writeTo(Serializer output) throws IOException {
      output.serializeRepeatedMessage(Resource.ATTRIBUTES, attributes);
    }

    private static int calculateSize(KeyValueMarshaler[] attributeMarshalers) {
      return MarshalerUtil.sizeRepeatedMessage(Resource.ATTRIBUTES, attributeMarshalers);
    }
  }
}
