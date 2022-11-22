/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.trace.data.EventData;
import org.junit.jupiter.api.Test;

class EventDataToAnnotationTest {

  @Test
  void basicConversion() {

    Attributes attrs =
        Attributes.builder()
            .put("v1", "v1")
            .put("v2", 12L)
            .put("v3", 123.45)
            .put("v4", false)
            .put("v5", "foo", "bar", "baz")
            .put("v6", 1, 2, 3)
            .put("v7", 1.23, 3.45)
            .put("v8", true, false, true)
            .build();
    String expected =
        "\"cat\":{\"v1\":\"v1\",\"v2\":12,\"v3\":123.45,\"v4\":false,\"v5\":[\"foo\",\"bar\",\"baz\"],\"v6\":[1,2,3],\"v7\":[1.23,3.45],\"v8\":[true,false,true]}";
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
