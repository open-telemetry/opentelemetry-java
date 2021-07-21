/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.context.internal.shaded.WeakConcurrentMap;
import io.opentelemetry.proto.resource.v1.Resource;

final class ResourceAdapter {

  private static final WeakConcurrentMap<io.opentelemetry.sdk.resources.Resource, Resource>
      RESOURCE_PROTO_CACHE = new WeakConcurrentMap.WithInlinedExpunction<>();

  static Resource toProtoResource(io.opentelemetry.sdk.resources.Resource resource) {
    Resource cached = RESOURCE_PROTO_CACHE.get(resource);
    if (cached == null) {
      // Since WeakConcurrentMap doesn't support computeIfAbsent, we may end up doing the conversion
      // a few times until the cache gets filled which is fine.
      Resource.Builder builder = Resource.newBuilder();
      resource
          .getAttributes()
          .forEach(
              (key, value) -> builder.addAttributes(CommonAdapter.toProtoAttribute(key, value)));
      cached = builder.build();
      RESOURCE_PROTO_CACHE.put(resource, cached);
    }
    return cached;
  }

  private ResourceAdapter() {}
}
