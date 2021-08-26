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
      cached = new ResourceMarshaler(AttributeMarshaler.createRepeated(resource.getAttributes()));
      RESOURCE_MARSHALER_CACHE.put(resource, cached);
    }
    return cached;
  }

  private ResourceMarshaler(AttributeMarshaler[] attributeMarshalers) {
    super(calculateSize(attributeMarshalers));
    ByteArrayOutputStream bos = new ByteArrayOutputStream(getSerializedSize());
    CodedOutputStream output = CodedOutputStream.newInstance(bos);
    try {
      MarshalerUtil.marshalRepeatedMessage(
          Resource.ATTRIBUTES_FIELD_NUMBER, attributeMarshalers, output);
      output.flush();
    } catch (IOException e) {
      // Presized so can't happen (we would have already thrown OutOfMemoryError)
      throw new UncheckedIOException(e);
    }
    serializedResource = bos.toByteArray();
  }

  @Override
  public void writeTo(CodedOutputStream output) throws IOException {
    output.writeRawBytes(serializedResource);
  }

  private static int calculateSize(AttributeMarshaler[] attributeMarshalers) {
    return MarshalerUtil.sizeRepeatedMessage(Resource.ATTRIBUTES_FIELD_NUMBER, attributeMarshalers);
  }
}
