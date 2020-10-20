/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import static io.opentelemetry.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.common.AttributeKey.booleanKey;
import static io.opentelemetry.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.common.AttributeKey.doubleKey;
import static io.opentelemetry.common.AttributeKey.longArrayKey;
import static io.opentelemetry.common.AttributeKey.longKey;
import static io.opentelemetry.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.common.AttributeKey.stringKey;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.testing.EqualsTester;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableAttributes;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Resource}. */
class ResourceTest {
  private static final Resource DEFAULT_RESOURCE = Resource.create(Attributes.empty());
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
    Attributes.Builder attributes = Attributes.builder();

    attributes.set(stringKey("string"), null);
    Resource resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isZero();
    attributes.set(stringArrayKey("stringArray"), Arrays.asList(null, "a"));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(1);

    attributes.set(booleanKey("bool"), true);
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(2);
    attributes.set(booleanArrayKey("boolArray"), Arrays.asList(null, true));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(3);

    attributes.set(longKey("long"), 0L);
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(4);
    attributes.set(longArrayKey("longArray"), Arrays.asList(1L, null));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(5);

    attributes.set(doubleKey("double"), 1.1);
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(6);
    attributes.set(doubleArrayKey("doubleArray"), Arrays.asList(1.1, null));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(7);
  }

  @Test
  void create_NullEmptyArray() {
    Attributes.Builder attributes = Attributes.builder();

    // Empty arrays should be maintained
    attributes.set(stringArrayKey("stringArrayAttribute"), Collections.emptyList());
    attributes.set(booleanArrayKey("boolArrayAttribute"), Collections.emptyList());
    attributes.set(longArrayKey("longArrayAttribute"), Collections.emptyList());
    attributes.set(doubleArrayKey("doubleArrayAttribute"), Collections.emptyList());

    Resource resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(4);

    // Arrays with null values should be maintained
    attributes.set(stringArrayKey("ArrayWithNullStringKey"), singletonList(null));
    attributes.set(longArrayKey("ArrayWithNullLongKey"), singletonList(null));
    attributes.set(doubleArrayKey("ArrayWithNullDoubleKey"), singletonList(null));
    attributes.set(booleanArrayKey("ArrayWithNullBooleanKey"), singletonList(null));

    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(8);

    // Null arrays should be dropped
    attributes.set(stringArrayKey("NullArrayStringKey"), null);
    attributes.set(longArrayKey("NullArrayLongKey"), null);
    attributes.set(doubleArrayKey("NullArrayDoubleKey"), null);
    attributes.set(booleanArrayKey("NullArrayBooleanKey"), null);

    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(8);

    attributes.set(stringKey("dropNullString"), null);
    attributes.set(longKey("dropNullLong"), null);
    attributes.set(doubleKey("dropNullDouble"), null);
    attributes.set(booleanKey("dropNullBool"), null);

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
        .testEquals();
  }

  @Test
  void testMergeResources() {
    Attributes expectedAttributes =
        Attributes.of(stringKey("a"), "1", stringKey("b"), "2", stringKey("c"), "4");

    Resource resource = DEFAULT_RESOURCE.merge(resource1).merge(resource2);
    assertThat(resource.getAttributes()).isEqualTo(expectedAttributes);
  }

  @Test
  void testMergeResources_Resource1() {
    Attributes expectedAttributes = Attributes.of(stringKey("a"), "1", stringKey("b"), "2");

    Resource resource = DEFAULT_RESOURCE.merge(resource1);
    assertThat(resource.getAttributes()).isEqualTo(expectedAttributes);
  }

  @Test
  void testMergeResources_Resource1_Null() {
    Attributes expectedAttributes =
        Attributes.of(
            stringKey("a"), "1",
            stringKey("b"), "3",
            stringKey("c"), "4");

    Resource resource = DEFAULT_RESOURCE.merge(null).merge(resource2);
    assertThat(resource.getAttributes()).isEqualTo(expectedAttributes);
  }

  @Test
  void testMergeResources_Resource2_Null() {
    Attributes expectedAttributes = Attributes.of(stringKey("a"), "1", stringKey("b"), "2");
    Resource resource = DEFAULT_RESOURCE.merge(resource1).merge(null);
    assertThat(resource.getAttributes()).isEqualTo(expectedAttributes);
  }

  @Test
  void testSdkTelemetryResources() {
    Resource resource = Resource.getTelemetrySdk();
    ReadableAttributes attributes = resource.getAttributes();
    assertThat(attributes.get(stringKey("telemetry.sdk.name"))).isEqualTo("opentelemetry");
    assertThat(attributes.get(stringKey("telemetry.sdk.language"))).isEqualTo("java");
    assertThat(attributes.get(stringKey("telemetry.sdk.version"))).isNotNull();
  }
}
