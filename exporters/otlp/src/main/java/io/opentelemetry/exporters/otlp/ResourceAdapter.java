/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporters.otlp;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.ReadableKeyValuePairs.KeyValueConsumer;
import io.opentelemetry.proto.resource.v1.Resource;

final class ResourceAdapter {
  static Resource toProtoResource(io.opentelemetry.sdk.resources.Resource resource) {
    final Resource.Builder builder = Resource.newBuilder();
    resource
        .getAttributes()
        .forEach(
            new KeyValueConsumer<AttributeValue>() {
              @Override
              public void consume(String key, AttributeValue value) {
                builder.addAttributes(CommonAdapter.toProtoAttribute(key, value));
              }
            });
    return builder.build();
  }

  private ResourceAdapter() {}
}
