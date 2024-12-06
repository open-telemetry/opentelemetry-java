/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.resources.Entity;
import io.opentelemetry.sdk.resources.Resource;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

public class ResourceEntityTest {
  @Test
  void toJsonResourceWithEntity() throws Exception {
    Resource resource =
        Resource.builder()
            .add(
                Entity.builder("test")
                    .setSchemaUrl("http://example.com/1.0")
                    .withIdentifying(attr -> attr.put("test.id", 1))
                    .withDescriptive(attr -> attr.put("test.name", "one"))
                    .build())
            .build();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      ResourceMarshaler.create(resource).writeJsonTo(out);
    } finally {
      out.close();
    }

    String json = new String(out.toByteArray(), StandardCharsets.UTF_8);
    assertThat(json)
        .isEqualTo(
            "{\"attributes\":[{\"key\":\"test.id\",\"value\":{\"intValue\":\"1\"}},{\"key\":\"test.name\",\"value\":{\"stringValue\":\"one\"}}],"
                + "\"entityRefs\":[{\"schemaUrl\":\"http://example.com/1.0\",\"type\":\"test\",\"idAttrKeys\":[\"test.id\"],\"descrAttrKeys\":[\"test.name\"]}]}");
  }
}
