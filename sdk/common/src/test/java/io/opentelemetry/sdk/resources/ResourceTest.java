/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import static io.opentelemetry.api.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.testing.EqualsTester;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Resource}. */
class ResourceTest {
  private Resource resource1;
  private Resource resource2;

  @BeforeEach
  void setUp() {
    Attributes attributes1 = Attributes.of(stringKey("a"), "1", stringKey("b"), "2");
    Attributes attribute2 =
        Attributes.of(stringKey("a"), "1", stringKey("b"), "3", stringKey("c"), "4");
    resource1 = Resource.create(attributes1);
    resource2 = Resource.create(attribute2);
  }

  @Test
  void create() {
    Attributes attributes = Attributes.of(stringKey("a"), "1", stringKey("b"), "2");
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
    AttributesBuilder attributes = Attributes.builder();

    attributes.put(stringKey("string"), null);
    Resource resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isZero();
    attributes.put(stringArrayKey("stringArray"), Arrays.asList(null, "a"));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(1);

    attributes.put(booleanKey("bool"), true);
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(2);
    attributes.put(booleanArrayKey("boolArray"), Arrays.asList(null, true));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(3);

    attributes.put(longKey("long"), 0L);
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(4);
    attributes.put(longArrayKey("longArray"), Arrays.asList(1L, null));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(5);

    attributes.put(doubleKey("double"), 1.1);
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(6);
    attributes.put(doubleArrayKey("doubleArray"), Arrays.asList(1.1, null));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(7);
  }

  @Test
  void create_NullEmptyArray() {
    AttributesBuilder attributes = Attributes.builder();

    // Empty arrays should be maintained
    attributes.put(stringArrayKey("stringArrayAttribute"), Collections.emptyList());
    attributes.put(booleanArrayKey("boolArrayAttribute"), Collections.emptyList());
    attributes.put(longArrayKey("longArrayAttribute"), Collections.emptyList());
    attributes.put(doubleArrayKey("doubleArrayAttribute"), Collections.emptyList());

    Resource resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(4);

    // Arrays with null values should be maintained
    attributes.put(stringArrayKey("ArrayWithNullStringKey"), singletonList(null));
    attributes.put(longArrayKey("ArrayWithNullLongKey"), singletonList(null));
    attributes.put(doubleArrayKey("ArrayWithNullDoubleKey"), singletonList(null));
    attributes.put(booleanArrayKey("ArrayWithNullBooleanKey"), singletonList(null));

    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(8);

    // Null arrays should be dropped
    attributes.put(stringArrayKey("NullArrayStringKey"), null);
    attributes.put(longArrayKey("NullArrayLongKey"), null);
    attributes.put(doubleArrayKey("NullArrayDoubleKey"), null);
    attributes.put(booleanArrayKey("NullArrayBooleanKey"), null);

    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(8);

    attributes.put(stringKey("dropNullString"), null);
    attributes.put(longKey("dropNullLong"), null);
    attributes.put(doubleKey("dropNullDouble"), null);
    attributes.put(booleanKey("dropNullBool"), null);

    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(8);
  }

  @Test
  void testResourceEquals() {
    Attributes attribute1 = Attributes.of(stringKey("a"), "1", stringKey("b"), "2");
    Attributes attribute2 =
        Attributes.of(stringKey("a"), "1", stringKey("b"), "3", stringKey("c"), "4");
    new EqualsTester()
        .addEqualityGroup(Resource.create(attribute1), Resource.create(attribute1), resource1)
        .addEqualityGroup(Resource.create(attribute2), resource2)
        .addEqualityGroup(
            Resource.create(attribute1, "http://schema"),
            Resource.create(attribute1, "http://schema"))
        .testEquals();
  }

  @Test
  void testMergeResources() {
    Attributes expectedAttributes =
        Attributes.of(stringKey("a"), "1", stringKey("b"), "3", stringKey("c"), "4");

    Resource resource = Resource.empty().merge(resource1).merge(resource2);
    assertThat(resource.getAttributes()).isEqualTo(expectedAttributes);
  }

  @Test
  void testMergeResources_schema() {
    Resource noSchemaOne = Resource.builder().put("a", 1).build();
    Resource noSchemaTwo = Resource.builder().put("b", 2).build();
    Resource schemaOne = Resource.builder().setSchemaUrl("http://schema.1").put("c", 3).build();
    Resource schemaTwo = Resource.builder().setSchemaUrl("http://schema.2").put("d", 4).build();
    Resource schemaTwoAgain =
        Resource.builder().setSchemaUrl("http://schema.2").put("e", 5).build();

    assertThat(noSchemaOne.merge(noSchemaTwo).getSchemaUrl()).isNull();
    assertThat(schemaOne.merge(noSchemaOne).getSchemaUrl()).isEqualTo(schemaOne.getSchemaUrl());
    assertThat(noSchemaOne.merge(schemaOne).getSchemaUrl()).isEqualTo(schemaOne.getSchemaUrl());
    assertThat(schemaTwo.merge(schemaTwoAgain).getSchemaUrl()).isEqualTo(schemaTwo.getSchemaUrl());
    assertThat(schemaOne.merge(schemaTwo).getSchemaUrl()).isNull();
    assertThat(schemaTwo.merge(schemaOne).getSchemaUrl()).isNull();
  }

  @Test
  void testMergeResources_Resource1() {
    Attributes expectedAttributes = Attributes.of(stringKey("a"), "1", stringKey("b"), "2");

    Resource resource = Resource.empty().merge(resource1);
    assertThat(resource.getAttributes()).isEqualTo(expectedAttributes);
  }

  @Test
  void testMergeResources_Resource1_Null() {
    Attributes expectedAttributes =
        Attributes.of(
            stringKey("a"), "1",
            stringKey("b"), "3",
            stringKey("c"), "4");

    Resource resource = Resource.empty().merge(null).merge(resource2);
    assertThat(resource.getAttributes()).isEqualTo(expectedAttributes);
  }

  @Test
  void testMergeResources_Resource2_Null() {
    Attributes expectedAttributes = Attributes.of(stringKey("a"), "1", stringKey("b"), "2");
    Resource resource = Resource.empty().merge(resource1).merge(null);
    assertThat(resource.getAttributes()).isEqualTo(expectedAttributes);
  }

  @Test
  void testDefaultResources() {
    Resource resource = Resource.getDefault();
    Attributes attributes = resource.getAttributes();
    assertThat(resource.getAttribute(ResourceAttributes.SERVICE_NAME))
        .isEqualTo("unknown_service:java");
    assertThat(resource.getAttribute(ResourceAttributes.TELEMETRY_SDK_NAME))
        .isEqualTo("opentelemetry");
    assertThat(resource.getAttribute(ResourceAttributes.TELEMETRY_SDK_LANGUAGE)).isEqualTo("java");
    assertThat(resource.getAttribute(ResourceAttributes.TELEMETRY_SDK_VERSION))
        .isEqualTo(System.getProperty("otel.test.project-version"));
  }

  @Test
  void shouldBuilderNotFailWithNullResource() {
    // given
    ResourceBuilder builder = Resource.getDefault().toBuilder();

    // when
    builder.putAll((Resource) null);

    // then no exception is thrown
    // and
    assertThat(builder.build().getAttribute(ResourceAttributes.SERVICE_NAME))
        .isEqualTo("unknown_service:java");
  }

  @Test
  void shouldBuilderCopyResource() {
    // given
    ResourceBuilder builder = Resource.getDefault().toBuilder();

    // when
    builder.put("dog says what?", "woof");

    // then
    Resource resource = builder.build();
    assertThat(resource).isNotSameAs(Resource.getDefault());
    assertThat(resource.getAttribute(stringKey("dog says what?"))).isEqualTo("woof");
  }

  @Test
  void shouldBuilderHelperMethodsBuildResource() {
    // given
    ResourceBuilder builder = Resource.getDefault().toBuilder();
    Attributes sourceAttributes = Attributes.of(stringKey("hello"), "world");
    Resource source = Resource.create(sourceAttributes);
    Attributes sourceAttributes2 = Attributes.of(stringKey("OpenTelemetry"), "Java");

    // when
    Resource resource =
        builder
            .put("long", 42L)
            .put("double", Math.E)
            .put("boolean", true)
            .put("string", "abc")
            .put("long array", 1L, 2L, 3L)
            .put("double array", Math.E, Math.PI)
            .put("boolean array", true, false)
            .put("string array", "first", "second")
            .put(longKey("long key"), 4242L)
            .put(longKey("int in disguise"), 21)
            .putAll(source)
            .putAll(sourceAttributes2)
            .build();

    // then
    Attributes attributes = resource.getAttributes();
    assertThat(attributes.get(longKey("long"))).isEqualTo(42L);
    assertThat(attributes.get(doubleKey("double"))).isEqualTo(Math.E);
    assertThat(attributes.get(booleanKey("boolean"))).isEqualTo(true);
    assertThat(attributes.get(stringKey("string"))).isEqualTo("abc");
    assertThat(attributes.get(longArrayKey("long array"))).isEqualTo(Arrays.asList(1L, 2L, 3L));
    assertThat(attributes.get(doubleArrayKey("double array")))
        .isEqualTo(Arrays.asList(Math.E, Math.PI));
    assertThat(attributes.get(booleanArrayKey("boolean array")))
        .isEqualTo(Arrays.asList(true, false));
    assertThat(attributes.get(stringArrayKey("string array")))
        .isEqualTo(Arrays.asList("first", "second"));
    assertThat(attributes.get(longKey("long key"))).isEqualTo(4242L);
    assertThat(attributes.get(longKey("int in disguise"))).isEqualTo(21);
    assertThat(attributes.get(stringKey("hello"))).isEqualTo("world");
    assertThat(attributes.get(stringKey("OpenTelemetry"))).isEqualTo("Java");
  }
}
