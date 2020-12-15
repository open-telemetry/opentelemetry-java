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
    // one extra from the service.name
    assertThat(resource.getAttributes().size()).isEqualTo(3);
    assertThat(resource.getAttributes().asMap()).containsAllEntriesOf(attributes.asMap());

    Resource resource1 = Resource.create(Attributes.empty());
    assertThat(resource1.getAttributes()).isNotNull();
    // one extra from the service.name
    assertThat(resource1.getAttributes().size()).isEqualTo(1);
  }

  @Test
  void create_ignoreNull() {
    AttributesBuilder attributes = Attributes.builder();

    attributes.put(stringKey("string"), null);
    Resource resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isOne();
    attributes.put(stringArrayKey("stringArray"), Arrays.asList(null, "a"));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    // one extra from the service.name
    assertThat(resource.getAttributes().size()).isEqualTo(2);

    attributes.put(booleanKey("bool"), true);
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    // one extra from the service.name
    assertThat(resource.getAttributes().size()).isEqualTo(3);
    attributes.put(booleanArrayKey("boolArray"), Arrays.asList(null, true));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    // one extra from the service.name
    assertThat(resource.getAttributes().size()).isEqualTo(4);

    attributes.put(longKey("long"), 0L);
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    // one extra from the service.name
    assertThat(resource.getAttributes().size()).isEqualTo(5);
    attributes.put(longArrayKey("longArray"), Arrays.asList(1L, null));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    // one extra from the service.name
    assertThat(resource.getAttributes().size()).isEqualTo(6);

    attributes.put(doubleKey("double"), 1.1);
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    // one extra from the service.name
    assertThat(resource.getAttributes().size()).isEqualTo(7);
    attributes.put(doubleArrayKey("doubleArray"), Arrays.asList(1.1, null));
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    // one extra from the service.name
    assertThat(resource.getAttributes().size()).isEqualTo(8);
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
    // one extra from the service.name
    assertThat(resource.getAttributes().size()).isEqualTo(5);

    // Arrays with null values should be maintained
    attributes.put(stringArrayKey("ArrayWithNullStringKey"), singletonList(null));
    attributes.put(longArrayKey("ArrayWithNullLongKey"), singletonList(null));
    attributes.put(doubleArrayKey("ArrayWithNullDoubleKey"), singletonList(null));
    attributes.put(booleanArrayKey("ArrayWithNullBooleanKey"), singletonList(null));

    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    // one extra from the service.name
    assertThat(resource.getAttributes().size()).isEqualTo(9);

    // Null arrays should be dropped
    attributes.put(stringArrayKey("NullArrayStringKey"), null);
    attributes.put(longArrayKey("NullArrayLongKey"), null);
    attributes.put(doubleArrayKey("NullArrayDoubleKey"), null);
    attributes.put(booleanArrayKey("NullArrayBooleanKey"), null);

    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    // one extra from the service.name
    assertThat(resource.getAttributes().size()).isEqualTo(9);

    attributes.put(stringKey("dropNullString"), null);
    attributes.put(longKey("dropNullLong"), null);
    attributes.put(doubleKey("dropNullDouble"), null);
    attributes.put(booleanKey("dropNullBool"), null);

    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    // one extra from the service.name
    assertThat(resource.getAttributes().size()).isEqualTo(9);
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
    assertThat(resource.getAttributes().asMap()).containsAllEntriesOf(expectedAttributes.asMap());
  }

  @Test
  void testMergeResources_Resource1() {
    Attributes expectedAttributes = Attributes.of(stringKey("a"), "1", stringKey("b"), "2");

    Resource resource = DEFAULT_RESOURCE.merge(resource1);
    assertThat(resource.getAttributes().asMap()).containsAllEntriesOf(expectedAttributes.asMap());
  }

  @Test
  void testMergeResources_Resource1_Null() {
    Attributes expectedAttributes =
        Attributes.of(
            stringKey("a"), "1",
            stringKey("b"), "3",
            stringKey("c"), "4");

    Resource resource = DEFAULT_RESOURCE.merge(null).merge(resource2);
    assertThat(resource.getAttributes().asMap()).containsAllEntriesOf(expectedAttributes.asMap());
  }

  @Test
  void testMergeResources_Resource2_Null() {
    Attributes expectedAttributes = Attributes.of(stringKey("a"), "1", stringKey("b"), "2");
    Resource resource = DEFAULT_RESOURCE.merge(resource1).merge(null);
    assertThat(resource.getAttributes().asMap()).containsAllEntriesOf(expectedAttributes.asMap());
  }

  @Test
  void testSdkTelemetryResources() {
    Resource resource = Resource.getTelemetrySdk();
    Attributes attributes = resource.getAttributes();
    assertThat(attributes.get(stringKey("telemetry.sdk.name"))).isEqualTo("opentelemetry");
    assertThat(attributes.get(stringKey("telemetry.sdk.language"))).isEqualTo("java");
    assertThat(attributes.get(stringKey("telemetry.sdk.version"))).isNotNull();
  }

  @Test
  void serviceNameFallback() {
    assertThat(Resource.getEmpty().getAttributes().get(ResourceAttributes.SERVICE_NAME))
        .startsWith("unknown_service:java");
    assertThat(
            Resource.create(Attributes.of(stringKey("foo"), "bar"))
                .getAttributes()
                .get(ResourceAttributes.SERVICE_NAME))
        .startsWith("unknown_service:java");

    assertThat(Resource.getDefault().getAttributes().get(ResourceAttributes.SERVICE_NAME))
        .startsWith("unknown_service:java");

    // make sure the fallback is only used if necessary
    assertThat(
            Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "my_service"))
                .getAttributes()
                .get(ResourceAttributes.SERVICE_NAME))
        .isEqualTo("my_service");

    // make sure that the fallback can be overridden by a merged Resource.
    assertThat(
            Resource.getDefault()
                .merge(
                    Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "my_service")))
                .getAttributes()
                .get(ResourceAttributes.SERVICE_NAME))
        .isEqualTo("my_service");

    assertThat(
            Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "my_service"))
                .merge(Resource.getDefault())
                .getAttributes()
                .get(ResourceAttributes.SERVICE_NAME))
        .isEqualTo("my_service");
  }
}
