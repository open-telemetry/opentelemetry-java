/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
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
  void create_ignoreNull() {
    Attributes.Builder attributes = Attributes.newBuilder();

    attributes.setAttribute("string", AttributeValue.stringAttributeValue(null));
    Resource resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isZero();
    attributes.setAttribute("stringArray", AttributeValue.arrayAttributeValue(null, "a"));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(1);

    attributes.setAttribute("bool", AttributeValue.booleanAttributeValue(true));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(2);
    attributes.setAttribute("boolArray", AttributeValue.arrayAttributeValue(null, true));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(3);

    attributes.setAttribute("long", AttributeValue.longAttributeValue(0L));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(4);
    attributes.setAttribute("longArray", AttributeValue.arrayAttributeValue(1L, null));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(5);

    attributes.setAttribute("double", AttributeValue.doubleAttributeValue(1.1));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(6);
    attributes.setAttribute("doubleArray", AttributeValue.arrayAttributeValue(1.1, null));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(7);
  }

  @Test
  void create_NullEmptyArray() {
    Attributes.Builder attributes = Attributes.newBuilder();

    // Empty arrays should be maintained
    attributes.setAttribute(
        "stringArrayAttribute", AttributeValue.arrayAttributeValue(new String[0]));
    attributes.setAttribute(
        "boolArrayAttribute", AttributeValue.arrayAttributeValue(new Boolean[0]));
    attributes.setAttribute("longArrayAttribute", AttributeValue.arrayAttributeValue(new Long[0]));
    attributes.setAttribute(
        "doubleArrayAttribute", AttributeValue.arrayAttributeValue(new Double[0]));

    Resource resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(4);

    // Arrays with null values should be maintained
    attributes.setAttribute(
        "ArrayWithNullStringKey", AttributeValue.arrayAttributeValue(new String[] {null}));
    attributes.setAttribute(
        "ArrayWithNullLongKey", AttributeValue.arrayAttributeValue(new Long[] {null}));
    attributes.setAttribute(
        "ArrayWithNullDoubleKey", AttributeValue.arrayAttributeValue(new Double[] {null}));
    attributes.setAttribute(
        "ArrayWithNullBooleanKey", AttributeValue.arrayAttributeValue(new Boolean[] {null}));

    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(8);

    // Null arrays should be dropped
    attributes.setAttribute(
        "NullArrayStringKey", AttributeValue.arrayAttributeValue((String[]) null));
    attributes.setAttribute("NullArrayLongKey", AttributeValue.arrayAttributeValue((Long[]) null));
    attributes.setAttribute(
        "NullArrayDoubleKey", AttributeValue.arrayAttributeValue((Double[]) null));
    attributes.setAttribute(
        "NullArrayBooleanKey", AttributeValue.arrayAttributeValue((Boolean[]) null));

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
