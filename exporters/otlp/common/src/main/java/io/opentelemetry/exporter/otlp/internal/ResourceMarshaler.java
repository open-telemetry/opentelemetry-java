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

  private final byte[] serializedResource;

  static ResourceMarshaler create(io.opentelemetry.sdk.resources.Resource resource) {
    ResourceMarshaler cached = RESOURCE_MARSHALER_CACHE.get(resource);
    if (cached == null) {
      // Since WeakConcurrentMap doesn't support computeIfAbsent, we may end up doing the conversion
      // a few times until the cache gets filled which is fine.
      cached = new ResourceMarshaler(KeyValueMarshaler.createRepeated(resource.getAttributes()));
      RESOURCE_MARSHALER_CACHE.put(resource, cached);
    }
    return cached;
  }

  private ResourceMarshaler(KeyValueMarshaler[] attributeMarshalers) {
    super(calculateSize(attributeMarshalers));
    ByteArrayOutputStream bos = new ByteArrayOutputStream(getProtoSerializedSize());
    CodedOutputStream output = CodedOutputStream.newInstance(bos);
    ProtoSerializer serializer = new ProtoSerializer(output);
    try {
      serializer.serializeRepeatedMessage(Resource.ATTRIBUTES, attributeMarshalers);
      output.flush();
    } catch (IOException e) {
      // Presized so can't happen (we would have already thrown OutOfMemoryError)
      throw new UncheckedIOException(e);
    }
    serializedResource = bos.toByteArray();
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    // TODO(anuraaga): Preserialize JSON as well.
    output.writeSerializedMessage(serializedResource, MarshalerUtil.EMPTY_BYTES);
  }

  private static int calculateSize(KeyValueMarshaler[] attributeMarshalers) {
    return MarshalerUtil.sizeRepeatedMessage(Resource.ATTRIBUTES, attributeMarshalers);
  }
}
