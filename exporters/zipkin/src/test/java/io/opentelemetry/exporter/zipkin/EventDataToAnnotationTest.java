/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin;

import static io.opentelemetry.api.common.AttributeKey.valueKey;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.KeyValue;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.sdk.trace.data.EventData;
import org.junit.jupiter.api.Test;

class EventDataToAnnotationTest {

  @Test
  void basicConversion() {

    Attributes attrs =
        Attributes.builder()
            .put("v01", "v1")
            .put("v02", 12L)
            .put("v03", 123.45)
            .put("v04", false)
            .put("v05", "foo", "bar", "baz")
            .put("v06", 1, 2, 3)
            .put("v07", 1.23, 3.45)
            .put("v08", true, false, true)
            .put(valueKey("v09"), Value.of(new byte[] {1, 2, 3}))
            .put(valueKey("v10"), Value.of(KeyValue.of("nested", Value.of("value"))))
            .put(valueKey("v11"), Value.of(Value.of("string"), Value.of(123L)))
            .put(valueKey("v12"), Value.empty())
            .build();
    String expected =
        "\"cat\":{\"v01\":\"v1\",\"v02\":12,\"v03\":123.45,\"v04\":false,\"v05\":[\"foo\",\"bar\",\"baz\"],\"v06\":[1,2,3],\"v07\":[1.23,3.45],\"v08\":[true,false,true],\"v09\":\"AQID\",\"v10\":{\"nested\":\"value\"},\"v11\":[\"string\",123],\"v12\":null}";
    EventData eventData = EventData.create(0, "cat", attrs);

    String result = EventDataToAnnotation.apply(eventData);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  void empty() {
    Attributes attrs = Attributes.empty();
    String expected = "\"dog\":{}";
    EventData eventData = EventData.create(0, "dog", attrs);

    String result = EventDataToAnnotation.apply(eventData);

    assertThat(result).isEqualTo(expected);
  }
}
