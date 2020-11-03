/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporters.otlp;

import io.opentelemetry.api.common.AttributeConsumer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.proto.resource.v1.Resource;

final class ResourceAdapter {
  static Resource toProtoResource(io.opentelemetry.sdk.resources.Resource resource) {
    final Resource.Builder builder = Resource.newBuilder();
    resource
        .getAttributes()
        .forEach(
            new AttributeConsumer() {
              @Override
              public <T> void accept(AttributeKey<T> key, T value) {
                builder.addAttributes(CommonAdapter.toProtoAttribute(key, value));
              }
            });
    return builder.build();
  }

  private ResourceAdapter() {}
}
