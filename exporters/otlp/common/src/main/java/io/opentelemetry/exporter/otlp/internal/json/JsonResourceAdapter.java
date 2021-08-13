/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.json;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.opentelemetry.context.internal.shaded.WeakConcurrentMap;
import io.opentelemetry.sdk.resources.Resource;

public final class JsonResourceAdapter {
  private static final WeakConcurrentMap<Resource, JSONObject> RESOURCE_PROTO_CACHE =
      new WeakConcurrentMap.WithInlinedExpunction<>();

  static JSONObject toProtoResource(Resource resource) {
    JSONObject cached = RESOURCE_PROTO_CACHE.get(resource);
    if (cached == null) {
      // Since WeakConcurrentMap doesn't support computeIfAbsent, we may end up doing the conversion
      // a few times until the cache gets filled which is fine.
      JSONObject builder = new JSONObject();
      JSONArray attributes = new JSONArray();
      resource
          .getAttributes()
          .forEach((key, value) -> attributes.add(JsonCommonAdapter.toJsonAttribute(key, value)));
      builder.put("attributes", attributes);
      cached = builder;
      RESOURCE_PROTO_CACHE.put(resource, cached);
    }
    return cached;
  }

  JsonResourceAdapter() {}
}
