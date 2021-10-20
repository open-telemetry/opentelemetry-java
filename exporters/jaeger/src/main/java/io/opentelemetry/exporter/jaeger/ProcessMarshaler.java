/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.jaeger;

import io.opentelemetry.exporter.jaeger.proto.api_v2.internal.Process;
import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.v1.resource.attributes.ResourceAttributes;
import java.io.IOException;
import java.util.List;

final class ProcessMarshaler extends MarshalerWithSize {

  private final byte[] serviceNameUtf8;
  private final List<KeyValueMarshaler> tags;

  static ProcessMarshaler create(Resource resource) {
    String serviceName = resource.getAttribute(ResourceAttributes.SERVICE_NAME);
    if (serviceName == null || serviceName.isEmpty()) {
      serviceName = Resource.getDefault().getAttribute(ResourceAttributes.SERVICE_NAME);
    }

    return new ProcessMarshaler(
        MarshalerUtil.toBytes(serviceName),
        KeyValueMarshaler.createRepeated(resource.getAttributes()));
  }

  ProcessMarshaler(byte[] serviceNameUtf8, List<KeyValueMarshaler> tags) {
    super(calculateSize(serviceNameUtf8, tags));
    this.serviceNameUtf8 = serviceNameUtf8;
    this.tags = tags;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeString(Process.SERVICE_NAME, serviceNameUtf8);
    output.serializeRepeatedMessage(Process.TAGS, tags);
  }

  private static int calculateSize(byte[] serviceNameUtf8, List<KeyValueMarshaler> tags) {
    int size = 0;
    size += MarshalerUtil.sizeBytes(Process.SERVICE_NAME, serviceNameUtf8);
    size += MarshalerUtil.sizeRepeatedMessage(Process.TAGS, tags);
    return size;
  }
}
