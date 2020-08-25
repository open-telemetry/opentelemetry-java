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

import static io.opentelemetry.common.AttributeValue.Factory.arrayAttributeValue;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.testing.EqualsTester;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Resource}. */
class ResourceTest {
  private static final Resource DEFAULT_RESOURCE = Resource.create(Attributes.Factory.empty());
  private Resource resource1;
  private Resource resource2;

  @BeforeEach
  void setUp() {
    Attributes attributes1 =
        Attributes.Factory.of(
            "a",
            AttributeValue.Factory.stringAttributeValue("1"),
            "b",
            AttributeValue.Factory.stringAttributeValue("2"));
    Attributes attribute2 =
        Attributes.Factory.of(
            "a",
            AttributeValue.Factory.stringAttributeValue("1"),
            "b",
            AttributeValue.Factory.stringAttributeValue("3"),
            "c",
            AttributeValue.Factory.stringAttributeValue("4"));
    resource1 = Resource.create(attributes1);
    resource2 = Resource.create(attribute2);
  }

  @Test
  void create() {
    Attributes attributes =
        Attributes.Factory.of(
            "a",
            AttributeValue.Factory.stringAttributeValue("1"),
            "b",
            AttributeValue.Factory.stringAttributeValue("2"));
    Resource resource = Resource.create(attributes);
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(2);
    assertThat(resource.getAttributes()).isEqualTo(attributes);

    Resource resource1 = Resource.create(Attributes.Factory.empty());
    assertThat(resource1.getAttributes()).isNotNull();
    assertThat(resource1.getAttributes().isEmpty()).isTrue();
  }

  @Test
  void create_ignoreNull() {
    Attributes.Builder attributes = Attributes.Factory.newBuilder();

    attributes.setAttribute("string", AttributeValue.Factory.stringAttributeValue(null));
    Resource resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isZero();
    attributes.setAttribute("stringArray", arrayAttributeValue(null, "a"));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(1);

    attributes.setAttribute("bool", AttributeValue.Factory.booleanAttributeValue(true));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(2);
    attributes.setAttribute("boolArray", arrayAttributeValue(null, true));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(3);

    attributes.setAttribute("long", AttributeValue.Factory.longAttributeValue(0L));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(4);
    attributes.setAttribute("longArray", arrayAttributeValue(1L, null));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(5);

    attributes.setAttribute("double", AttributeValue.Factory.doubleAttributeValue(1.1));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(6);
    attributes.setAttribute("doubleArray", arrayAttributeValue(1.1, null));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(7);
  }

  @Test
  void create_NullEmptyArray() {
    Attributes.Builder attributes = Attributes.Factory.newBuilder();

    // Empty arrays should be maintained
    attributes.setAttribute("stringArrayAttribute", arrayAttributeValue(new String[0]));
    attributes.setAttribute("boolArrayAttribute", arrayAttributeValue(new Boolean[0]));
    attributes.setAttribute("longArrayAttribute", arrayAttributeValue(new Long[0]));
    attributes.setAttribute("doubleArrayAttribute", arrayAttributeValue(new Double[0]));

    Resource resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(4);

    // Arrays with null values should be maintained
    attributes.setAttribute("ArrayWithNullStringKey", arrayAttributeValue(new String[] {null}));
    attributes.setAttribute("ArrayWithNullLongKey", arrayAttributeValue(new Long[] {null}));
    attributes.setAttribute("ArrayWithNullDoubleKey", arrayAttributeValue(new Double[] {null}));
    attributes.setAttribute("ArrayWithNullBooleanKey", arrayAttributeValue(new Boolean[] {null}));

    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(8);

    // Null arrays should be dropped
    attributes.setAttribute("NullArrayStringKey", arrayAttributeValue((String[]) null));
    attributes.setAttribute("NullArrayLongKey", arrayAttributeValue((Long[]) null));
    attributes.setAttribute("NullArrayDoubleKey", arrayAttributeValue((Double[]) null));
    attributes.setAttribute("NullArrayBooleanKey", arrayAttributeValue((Boolean[]) null));

    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(8);

    attributes.setAttribute("dropNullString", (AttributeValue) null);
    attributes.setAttribute("dropNullLong", (AttributeValue) null);
    attributes.setAttribute("dropNullDouble", (AttributeValue) null);
    attributes.setAttribute("dropNullBool", (AttributeValue) null);

    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(8);
  }

  @Test
  void testResourceEquals() {
    Attributes attribute1 =
        Attributes.Factory.of(
            "a",
            AttributeValue.Factory.stringAttributeValue("1"),
            "b",
            AttributeValue.Factory.stringAttributeValue("2"));
    Attributes attribute2 =
        Attributes.Factory.of(
            "a",
            AttributeValue.Factory.stringAttributeValue("1"),
            "b",
            AttributeValue.Factory.stringAttributeValue("3"),
            "c",
            AttributeValue.Factory.stringAttributeValue("4"));
    new EqualsTester()
        .addEqualityGroup(Resource.create(attribute1), Resource.create(attribute1), resource1)
        .addEqualityGroup(Resource.create(attribute2), resource2)
        .testEquals();
  }

  @Test
  void testMergeResources() {
    Attributes expectedAttributes =
        Attributes.Factory.of(
            "a",
            AttributeValue.Factory.stringAttributeValue("1"),
            "b",
            AttributeValue.Factory.stringAttributeValue("2"),
            "c",
            AttributeValue.Factory.stringAttributeValue("4"));

    Resource resource = DEFAULT_RESOURCE.merge(resource1).merge(resource2);
    assertThat(resource.getAttributes()).isEqualTo(expectedAttributes);
  }

  @Test
  void testMergeResources_Resource1() {
    Attributes expectedAttributes =
        Attributes.Factory.of(
            "a",
            AttributeValue.Factory.stringAttributeValue("1"),
            "b",
            AttributeValue.Factory.stringAttributeValue("2"));

    Resource resource = DEFAULT_RESOURCE.merge(resource1);
    assertThat(resource.getAttributes()).isEqualTo(expectedAttributes);
  }

  @Test
  void testMergeResources_Resource1_Null() {
    Attributes expectedAttributes =
        Attributes.Factory.of(
            "a", AttributeValue.Factory.stringAttributeValue("1"),
            "b", AttributeValue.Factory.stringAttributeValue("3"),
            "c", AttributeValue.Factory.stringAttributeValue("4"));

    Resource resource = DEFAULT_RESOURCE.merge(null).merge(resource2);
    assertThat(resource.getAttributes()).isEqualTo(expectedAttributes);
  }

  @Test
  void testMergeResources_Resource2_Null() {
    Attributes expectedAttributes =
        Attributes.Factory.of(
            "a",
            AttributeValue.Factory.stringAttributeValue("1"),
            "b",
            AttributeValue.Factory.stringAttributeValue("2"));
    Resource resource = DEFAULT_RESOURCE.merge(resource1).merge(null);
    assertThat(resource.getAttributes()).isEqualTo(expectedAttributes);
  }

  @Test
  void testSdkTelemetryResources() {
    Resource resource = Resource.getTelemetrySdk();
    ReadableAttributes attributes = resource.getAttributes();
    assertThat(attributes.get("telemetry.sdk.name"))
        .isEqualTo(AttributeValue.Factory.stringAttributeValue("opentelemetry"));
    assertThat(attributes.get("telemetry.sdk.language"))
        .isEqualTo(AttributeValue.Factory.stringAttributeValue("java"));
    assertThat(attributes.get("telemetry.sdk.version").getStringValue()).isNotNull();
  }
}
