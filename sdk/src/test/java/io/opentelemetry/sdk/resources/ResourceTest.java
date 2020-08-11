/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.resources;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.testing.EqualsTester;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Resource}. */
class ResourceTest {
  private static final Resource DEFAULT_RESOURCE = Resource.create(Attributes.empty());
  private Resource resource1;
  private Resource resource2;

  @BeforeEach
  void setUp() {
    Attributes attributes1 =
        Attributes.of(
            "a",
            AttributeValue.stringAttributeValue("1"),
            "b",
            AttributeValue.stringAttributeValue("2"));
    Attributes attribute2 =
        Attributes.of(
            "a",
            AttributeValue.stringAttributeValue("1"),
            "b",
            AttributeValue.stringAttributeValue("3"),
            "c",
            AttributeValue.stringAttributeValue("4"));
    resource1 = Resource.create(attributes1);
    resource2 = Resource.create(attribute2);
  }

  @Test
  void create() {
    Attributes attributes =
        Attributes.of(
            "a",
            AttributeValue.stringAttributeValue("1"),
            "b",
            AttributeValue.stringAttributeValue("2"));
    Resource resource = Resource.create(attributes);
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(2);
    assertThat(resource.getAttributes()).isEqualTo(attributes);

    Resource resource1 = Resource.create(Attributes.empty());
    assertThat(resource1.getAttributes()).isNotNull();
    assertThat(resource1.getAttributes().isEmpty()).isTrue();
  }

  @Test
  void testResourceEquals() {
    Attributes attribute1 =
        Attributes.of(
            "a",
            AttributeValue.stringAttributeValue("1"),
            "b",
            AttributeValue.stringAttributeValue("2"));
    Attributes attribute2 =
        Attributes.of(
            "a",
            AttributeValue.stringAttributeValue("1"),
            "b",
            AttributeValue.stringAttributeValue("3"),
            "c",
            AttributeValue.stringAttributeValue("4"));
    new EqualsTester()
        .addEqualityGroup(Resource.create(attribute1), Resource.create(attribute1), resource1)
        .addEqualityGroup(Resource.create(attribute2), resource2)
        .testEquals();
  }

  @Test
  void testMergeResources() {
    Attributes expectedAttributes =
        Attributes.of(
            "a",
            AttributeValue.stringAttributeValue("1"),
            "b",
            AttributeValue.stringAttributeValue("2"),
            "c",
            AttributeValue.stringAttributeValue("4"));

    Resource resource = DEFAULT_RESOURCE.merge(resource1).merge(resource2);
    assertThat(resource.getAttributes()).isEqualTo(expectedAttributes);
  }

  @Test
  void testMergeResources_Resource1() {
    Attributes expectedAttributes =
        Attributes.of(
            "a",
            AttributeValue.stringAttributeValue("1"),
            "b",
            AttributeValue.stringAttributeValue("2"));

    Resource resource = DEFAULT_RESOURCE.merge(resource1);
    assertThat(resource.getAttributes()).isEqualTo(expectedAttributes);
  }

  @Test
  void testMergeResources_Resource1_Null() {
    Attributes expectedAttributes =
        Attributes.of(
            "a", AttributeValue.stringAttributeValue("1"),
            "b", AttributeValue.stringAttributeValue("3"),
            "c", AttributeValue.stringAttributeValue("4"));

    Resource resource = DEFAULT_RESOURCE.merge(null).merge(resource2);
    assertThat(resource.getAttributes()).isEqualTo(expectedAttributes);
  }

  @Test
  void testMergeResources_Resource2_Null() {
    Attributes expectedAttributes =
        Attributes.of(
            "a",
            AttributeValue.stringAttributeValue("1"),
            "b",
            AttributeValue.stringAttributeValue("2"));
    Resource resource = DEFAULT_RESOURCE.merge(resource1).merge(null);
    assertThat(resource.getAttributes()).isEqualTo(expectedAttributes);
  }

  @Test
  void testSdkTelemetryResources() {
    Resource resource = Resource.getTelemetrySdk();
    ReadableAttributes attributes = resource.getAttributes();
    assertThat(attributes.get("telemetry.sdk.name"))
        .isEqualTo(AttributeValue.stringAttributeValue("opentelemetry"));
    assertThat(attributes.get("telemetry.sdk.language"))
        .isEqualTo(AttributeValue.stringAttributeValue("java"));
    assertThat(attributes.get("telemetry.sdk.version").getStringValue()).isNotNull();
  }
}
