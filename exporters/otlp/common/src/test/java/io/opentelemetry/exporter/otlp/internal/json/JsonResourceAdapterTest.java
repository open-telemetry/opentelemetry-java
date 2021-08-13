/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.json;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

class JsonResourceAdapterTest {
  @Test
  void toProtoResource() {
    Resource resource =
        Resource.create(
            Attributes.of(
                booleanKey("key_bool"),
                true,
                stringKey("key_string"),
                "string",
                longKey("key_int"),
                100L,
                doubleKey("key_double"),
                100.3));
    JSONObject protoResource = JsonResourceAdapter.toProtoResource(resource);
    // Memoized
    assertThat(JsonResourceAdapter.toProtoResource(resource)).isSameAs(protoResource);
  }

  @Test
  void toProtoResource_Empty() {
    JSONObject protoResource = new JSONObject();
    JSONArray attributes = new JSONArray();
    protoResource.put("attributes", attributes);
    assertThat(JsonResourceAdapter.toProtoResource(Resource.empty())).isEqualTo(protoResource);
  }
}
