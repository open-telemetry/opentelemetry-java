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
import static io.opentelemetry.api.common.AttributeKey.valueKey;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.slf4j.event.Level.WARN;

import com.google.common.testing.EqualsTester;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributeType;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.resources.internal.Entity;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests for {@link Resource}. */
@SuppressLogger(Resource.class)
@SuppressLogger(ResourceBuilder.class)
class ResourceTest {
  @RegisterExtension
  LogCapturer logs =
      LogCapturer.create().captureForType(ResourceBuilder.class).captureForType(Resource.class);

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

  @ParameterizedTest
  @MethodSource("entityInsertionOperations")
  void entityInsertionDefersPendingAttributeValidation(Consumer<ResourceBuilder> insert) {
    ResourceBuilder builder = Resource.builder().put("invalid-é", "pending");
    insert.accept(builder);

    assertThatThrownBy(builder::build).isInstanceOf(IllegalArgumentException.class);
    Resource filtered = builder.removeIf(key -> key.getKey().equals("invalid-é")).build();
    assertThat(filtered.getEntities()).hasSize(1);
    assertThat(filtered.getAttributes()).isEqualTo(Attributes.of(stringKey("host.id"), "H1"));
  }

  static Stream<Arguments> entityInsertionOperations() {
    Entity host = Entity.builder("host", Attributes.of(stringKey("host.id"), "H1")).build();
    return Stream.of(
        Arguments.argumentSet(
            "direct insertion", (Consumer<ResourceBuilder>) builder -> builder.addEntity(host)),
        Arguments.argumentSet(
            "resource copy",
            (Consumer<ResourceBuilder>)
                builder -> builder.putAll(EntityUtil.createResource(singletonList(host)))));
  }

  @Test
  void jointCreationAppliesAttributesBeforeEntities() {
    Entity host =
        Entity.builder("host", Attributes.of(stringKey("host.id"), "H1"))
            .setDescription(Attributes.of(stringKey("host.name"), "machine"))
            .build();
    Attributes attributes =
        Attributes.builder()
            .put("host.id", 2L)
            .put("host.name", "earlier")
            .put("env", "prod")
            .build();
    Resource resource = Resource.builder().putAll(attributes).addEntity(host).build();

    assertThat(resource.getEntities()).containsExactly(host);
    assertThat(resource.getAttributes())
        .isEqualTo(
            Attributes.builder()
                .put("host.id", "H1")
                .put("host.name", "machine")
                .put("env", "prod")
                .build());
    assertThat(resource)
        .isEqualTo(
            Resource.create(attributes).merge(EntityUtil.createResource(singletonList(host))));
  }

  @ParameterizedTest
  @MethodSource("overlappingRawAttributes")
  void normalizedAssemblerRejectsOverlappingAttributes(Attributes attributes) {
    Entity host =
        Entity.builder("host", Attributes.of(stringKey("host.id"), "H1"))
            .setDescription(Attributes.of(stringKey("host.name"), "machine"))
            .build();
    assertThatThrownBy(() -> Resource.create(attributes, null, singletonList(host)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unassociated attributes must not overlap entity attributes.");
  }

  static Stream<Arguments> overlappingRawAttributes() {
    return Stream.of(
        Arguments.argumentSet(
            "unchanged identifying value", Attributes.of(stringKey("host.id"), "H1")),
        Arguments.argumentSet(
            "changed identifying value", Attributes.of(stringKey("host.id"), "H2")),
        Arguments.argumentSet("changed identifying type", Attributes.of(longKey("host.id"), 1L)),
        Arguments.argumentSet(
            "descriptive attribute key", Attributes.of(stringKey("host.name"), "other")));
  }

  @Test
  void normalizedAssemblerRejectsDuplicateTypesAndAttributeOwners() {
    Entity host = Entity.builder("host", Attributes.of(stringKey("host.id"), "H1")).build();
    Entity otherHost = Entity.builder("host", Attributes.of(stringKey("other.id"), "H2")).build();
    Entity process = Entity.builder("process", Attributes.of(stringKey("host.id"), "P1")).build();
    assertThatThrownBy(
            () -> Resource.create(Attributes.empty(), null, Arrays.asList(host, otherHost)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Entity types must be unique in normalized resources.");
    assertThatThrownBy(
            () -> Resource.create(Attributes.empty(), null, Arrays.asList(host, process)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Entity attribute keys must have a single owner and classification.");
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
  void builder_ignoreNull() {
    Resource resource =
        Resource.builder()
            .put((String) null, "cat")
            .put("bear", (String) null)
            .put(null, 1.0)
            .put(null, false)
            .put(null, "foo", "bar")
            .put("dog", (String[]) null)
            .put(null, 1.0, 2.0)
            .put("mouse", (double[]) null)
            .put(null, true, false)
            .put("elephant", (boolean[]) null)
            .put((AttributeKey<String>) null, "foo")
            .put(stringKey("monkey"), null)
            .put(stringKey(null), "foo")
            .put(stringKey(""), "foo")
            .put((AttributeKey<Long>) null, 10)
            .put(longKey(null), 10)
            .put(longKey(""), 10)
            .putAll((Attributes) null)
            .putAll((Resource) null)
            .setSchemaUrl(null)
            .build();

    assertThat(resource).isEqualTo(Resource.empty());
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
    attributes.put(stringArrayKey("NullArrayStringKey"), (String[]) null);
    attributes.put(longArrayKey("NullArrayLongKey"), (Long[]) null);
    attributes.put(doubleArrayKey("NullArrayDoubleKey"), (Double[]) null);
    attributes.put(booleanArrayKey("NullArrayBooleanKey"), (Boolean[]) null);

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
  void create_NullEmptyValue() {
    AttributesBuilder attributes = Attributes.builder();

    // Empty values should be maintained
    attributes.put(valueKey("value"), Value.empty());

    Resource resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(1);

    // Null values should be dropped
    attributes.put(valueKey("dropNullValue"), null);
    resource = Resource.create(attributes.build());
    assertThat(resource.getAttributes()).isNotNull();
    assertThat(resource.getAttributes().size()).isEqualTo(1);
  }

  @Test
  void create_invalidAttributeKey() {
    assertThatThrownBy(() -> Resource.create(Attributes.of(stringKey("\u0002ab"), "value")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Attribute key should be a ASCII string");

    assertThatThrownBy(() -> Resource.create(Attributes.of(stringKey("key\u007f"), "value")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Attribute key should be a ASCII string");

    char[] chars = new char[256];
    Arrays.fill(chars, 'a');
    String tooLongKey = new String(chars);
    assertThatThrownBy(() -> Resource.create(Attributes.of(stringKey(tooLongKey), "value")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Attribute key should be a ASCII string");
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
  void testToString() {
    Attributes attribute1 = Attributes.of(stringKey("a"), "1", stringKey("b"), "2");
    Resource resource = Resource.create(attribute1, "http://schema");
    assertThat(resource.toString())
        .isEqualTo("Resource{schemaUrl=http://schema, entities=[], attributes={a=\"1\", b=\"2\"}}");
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
  void testMergeResources_emptySchemaIsPresent() {
    Resource schemaOne = Resource.create(Attributes.of(stringKey("a"), "1"), "http://schema.1");
    Resource emptySchema = Resource.create(Attributes.of(stringKey("b"), "2"), "");

    assertThat(schemaOne.merge(emptySchema).getSchemaUrl()).isNull();
    assertThat(emptySchema.merge(schemaOne).getSchemaUrl()).isNull();
    assertThat(emptySchema.merge(emptySchema).getSchemaUrl()).isEmpty();
  }

  @Test
  void testMergeResources_entitySchemas() {
    Entity host =
        Entity.builder("host", Attributes.of(stringKey("host.id"), "H1"))
            .setSchemaUrl("S1")
            .build();
    Entity process =
        Entity.builder("process", Attributes.of(longKey("process.pid"), 1L))
            .setSchemaUrl("S2")
            .build();
    Entity withoutSchema =
        Entity.builder("service", Attributes.of(stringKey("service.name"), "svc")).build();
    Resource pinned =
        Resource.builder().addEntity(host).build().toBuilder().addEntity(process).build();
    Resource explicit = Resource.builder().setSchemaUrl("explicit").addEntity(host).build();
    Resource withoutEntitySchema = Resource.builder().addEntity(withoutSchema).build();

    assertThat(pinned.getSchemaUrl()).isEqualTo("S1");
    assertThat(Resource.empty().merge(pinned)).isEqualTo(pinned);
    assertThat(Resource.getDefault().merge(explicit).getSchemaUrl()).isEqualTo("explicit");
    assertThat(withoutEntitySchema.getSchemaUrl()).isNull();
    assertThat(
            Resource.create(Attributes.of(stringKey("a"), "1"), "S1")
                .merge(withoutEntitySchema)
                .getSchemaUrl())
        .isEqualTo("S1");
    assertThat(Resource.builder().addEntity(host).addEntity(withoutSchema).build().getSchemaUrl())
        .isEqualTo("S1");
  }

  @Test
  void testMergeResources_entityReplacementUsesResolvedSchemas() {
    Resource base =
        Resource.builder()
            .addEntity(
                Entity.builder("host", Attributes.of(stringKey("host.id"), "H1"))
                    .setSchemaUrl("S1")
                    .build())
            .build();
    Resource replacement =
        Resource.builder()
            .addEntity(
                Entity.builder("host", Attributes.of(stringKey("host.id"), "H2"))
                    .setSchemaUrl("S2")
                    .build())
            .build();

    assertThat(base.getSchemaUrl()).isEqualTo("S1");
    assertThat(replacement.getSchemaUrl()).isEqualTo("S2");
    assertThat(base.merge(replacement).getSchemaUrl()).isNull();
  }

  @Test
  void entitySnapshotsAreImmutable() {
    Entity host = Entity.builder("host", Attributes.of(stringKey("host.id"), "H1")).build();
    Entity service =
        Entity.builder("service", Attributes.of(stringKey("service.name"), "S1")).build();
    ResourceBuilder builder = Resource.builder().addEntity(host);
    Resource snapshot = builder.build();
    int hashCode = snapshot.hashCode();

    builder.addEntity(service).put("raw", "later").build();

    assertThat(snapshot.getEntities()).containsExactly(host);
    assertThat(snapshot.getAttributes()).isEqualTo(host.getId());
    assertThat(snapshot.hashCode()).isEqualTo(hashCode);
    assertThatThrownBy(() -> snapshot.getEntities().clear())
        .isInstanceOf(UnsupportedOperationException.class);

    List<Entity> input = new ArrayList<>(singletonList(host));
    Resource fromCollection = Resource.create(Attributes.empty(), null, input);
    input.clear();
    assertThat(fromCollection).isEqualTo(snapshot);
  }

  @Test
  void nullFilteringIsANoopWithAndWithoutEntities() {
    Entity host = Entity.builder("host", Attributes.of(stringKey("host.id"), "H1")).build();
    for (Resource original :
        Arrays.asList(
            Resource.create(Attributes.of(stringKey("ordinary"), "value")),
            EntityUtil.createResource(singletonList(host)))) {
      assertThat(original.toBuilder().removeIf(null).build()).isEqualTo(original);
    }
    logs.assertDoesNotContain("Removing entity association");
  }

  @Test
  void mergeDoesNotMutateInputs() {
    Entity host = Entity.builder("host", Attributes.of(stringKey("host.id"), "H1")).build();
    Resource base = Resource.create(Attributes.empty(), null, singletonList(host));
    Resource updating = Resource.create(Attributes.of(stringKey("host.id"), "H2"));

    Resource merged = base.merge(updating);

    assertThat(merged.getEntities()).isEmpty();
    assertThat(merged.getAttributes()).isEqualTo(updating.getAttributes());
    assertThat(base.getEntities()).containsExactly(host);
    assertThat(base.getAttributes()).isEqualTo(host.getId());
    assertThat(updating.getEntities()).isEmpty();
    assertThat(updating.getAttributes()).isEqualTo(Attributes.of(stringKey("host.id"), "H2"));
    assertThat(Resource.empty().merge(base)).isEqualTo(base);
  }

  @ParameterizedTest
  @MethodSource("entityAttributeOverrideCases")
  void entityAttributeOverride(Consumer<ResourceBuilder> override, Attributes expectedOverride) {
    Entity entity =
        Entity.builder("test", Attributes.of(stringKey("target"), "original"))
            .setDescription(Attributes.of(stringKey("retained"), "value"))
            .build();
    Resource original = Resource.builder().addEntity(entity).build();
    ResourceBuilder builder = original.toBuilder();

    override.accept(builder);
    Resource resource = builder.build();

    assertThat(resource.getEntities()).isEmpty();
    assertThat(resource.getAttributes())
        .isEqualTo(Attributes.builder().put("retained", "value").putAll(expectedOverride).build());
    logs.assertContains(
        event -> event.getLevel().equals(WARN), "Removing entity association [test]");
  }

  static Stream<Arguments> entityAttributeOverrideCases() {
    return Stream.of(
        overrideCase(
            "string",
            b -> b.put("target", "updated"),
            Attributes.of(stringKey("target"), "updated")),
        overrideCase("long", b -> b.put("target", 2L), Attributes.of(longKey("target"), 2L)),
        overrideCase("double", b -> b.put("target", 2.0), Attributes.of(doubleKey("target"), 2.0)),
        overrideCase(
            "boolean", b -> b.put("target", true), Attributes.of(booleanKey("target"), true)),
        overrideCase(
            "string array",
            b -> b.put("target", "updated", "next"),
            Attributes.of(stringArrayKey("target"), Arrays.asList("updated", "next"))),
        overrideCase(
            "long array",
            b -> b.put("target", 2L, 3L),
            Attributes.of(longArrayKey("target"), Arrays.asList(2L, 3L))),
        overrideCase(
            "double array",
            b -> b.put("target", 2.0, 3.0),
            Attributes.of(doubleArrayKey("target"), Arrays.asList(2.0, 3.0))),
        overrideCase(
            "boolean array",
            b -> b.put("target", true, false),
            Attributes.of(booleanArrayKey("target"), Arrays.asList(true, false))),
        overrideCase(
            "typed key",
            b -> b.put(stringKey("target"), "updated"),
            Attributes.of(stringKey("target"), "updated")),
        overrideCase(
            "typed int", b -> b.put(longKey("target"), 2), Attributes.of(longKey("target"), 2L)),
        overrideCase(
            "attributes",
            b -> b.putAll(Attributes.of(stringKey("target"), "updated")),
            Attributes.of(stringKey("target"), "updated")),
        overrideCase(
            "resource",
            b -> b.putAll(Resource.create(Attributes.of(stringKey("target"), "updated"))),
            Attributes.of(stringKey("target"), "updated")),
        overrideCase(
            "multiple attributes",
            b ->
                b.putAll(
                    Attributes.builder().put("target", "updated").put("retained", "new").build()),
            Attributes.builder().put("target", "updated").put("retained", "new").build()),
        overrideCase(
            "multiple puts",
            b -> b.put("target", "updated").put("retained", "new"),
            Attributes.builder().put("target", "updated").put("retained", "new").build()));
  }

  private static Arguments overrideCase(
      String name, Consumer<ResourceBuilder> override, Attributes expected) {
    return Arguments.argumentSet(name, override, expected);
  }

  @ParameterizedTest
  @MethodSource("entityAttributeOverrideCases")
  void unchangedEntityAttributesPreserveAssociations(
      Consumer<ResourceBuilder> write, Attributes attributes) {
    for (boolean identifying : new boolean[] {true, false}) {
      Entity entity =
          Entity.builder(
                  "test", identifying ? attributes : Attributes.of(stringKey("identity"), "E1"))
              .setDescription(
                  identifying ? Attributes.of(stringKey("other"), "retained") : attributes)
              .build();
      Resource original = Resource.builder().addEntity(entity).build();
      ResourceBuilder builder = original.toBuilder();

      write.accept(builder);

      assertThat(builder.build()).isEqualTo(original);
      assertThat(builder.build().getEntities()).containsExactly(entity);
      logs.assertDoesNotContain("Removing entity association");
    }
  }

  @Test
  void mixedUnchangedAndChangedAttributes() {
    Entity host =
        Entity.builder("host", Attributes.of(stringKey("host.id"), "H1"))
            .setDescription(Attributes.of(stringKey("host.name"), "old"))
            .build();
    Entity service =
        Entity.builder("service", Attributes.of(stringKey("service.name"), "S1")).build();
    Resource original = Resource.builder().addEntity(host).addEntity(service).build();
    Attributes incoming =
        Attributes.builder()
            .put("host.id", "H1")
            .put("host.name", "new")
            .put("service.name", "S1")
            .put("raw", "new")
            .build();
    Resource expected = original.merge(Resource.create(incoming));

    assertThat(expected.getEntities()).containsExactly(service);
    assertThat(expected.getAttributes()).isEqualTo(incoming);
    assertThat(original.toBuilder().putAll(incoming).build()).isEqualTo(expected);
    assertThat(original.toBuilder().putAll(Resource.create(incoming)).build()).isEqualTo(expected);
    assertThat(
            original.toBuilder()
                .put("host.id", "H1")
                .put("host.name", "new")
                .put("service.name", "S1")
                .put("raw", "new")
                .build())
        .isEqualTo(expected);
    logs.assertContains(
        event -> event.getLevel().equals(WARN), "Removing entity association [host]");
    logs.assertDoesNotContain("Removing entity association [service]");
  }

  @Test
  void unchangedCoercedValuePreservesAssociation() {
    Entity entity = Entity.builder("test", Attributes.of(stringKey("target"), "same")).build();
    Resource original = Resource.builder().addEntity(entity).build();
    Resource resource =
        original.toBuilder()
            .put(valueKey("target"), Value.of("same"))
            .putAll(Attributes.of(valueKey("target"), Value.of("same")))
            .build();
    assertThat(resource).isEqualTo(original);
    logs.assertDoesNotContain("Removing entity association");
  }

  @Test
  void ignoredNullOverridesDoNotRemoveAssociations() {
    Entity entity = Entity.builder("test", Attributes.of(stringKey("target"), "original")).build();
    Resource original = Resource.builder().addEntity(entity).build();
    Resource resource =
        original.toBuilder()
            .put("target", (String) null)
            .put(stringKey("target"), null)
            .put("target", (long[]) null)
            .putAll((Attributes) null)
            .build();
    assertThat(resource).isEqualTo(original);
  }

  @Test
  void entityOperationOrdering() {
    Entity host =
        Entity.builder("host", Attributes.of(stringKey("host.id"), "H1"))
            .setDescription(Attributes.of(stringKey("host.name"), "detected"))
            .build();
    ResourceBuilder builder = Resource.builder().put("host.name", "earlier").addEntity(host);
    assertThat(builder.build().getAttribute(stringKey("host.name"))).isEqualTo("detected");
    builder.put("host.name", "override");
    assertThat(builder.build().getEntities()).isEmpty();
    builder.addEntity(host);
    assertThat(builder.build().getEntities()).containsExactly(host);
    assertThat(builder.build().getAttribute(stringKey("host.name"))).isEqualTo("detected");
  }

  @Test
  void filterEntityAttributes() {
    Entity host =
        Entity.builder(
                "host",
                Attributes.builder().put("host.id", "H1").put("host.region", "west").build())
            .setDescription(
                Attributes.builder().put("host.name", "machine").put("host.type", "vm").build())
            .build();
    Resource original = Resource.builder().addEntity(host).put("raw", "remove").build();
    Resource filtered =
        original.toBuilder()
            .removeIf(
                key ->
                    key.getKey().equals("host.id")
                        || key.getKey().equals("host.name")
                        || key.getKey().equals("raw"))
            .build();

    assertThat(filtered.getEntities()).isEmpty();
    assertThat(filtered.getUnassociatedAttributes()).isEqualTo(filtered.getAttributes());
    assertThat(filtered.getAttributes())
        .isEqualTo(Attributes.builder().put("host.region", "west").put("host.type", "vm").build());
    logs.assertContains(
        event -> event.getLevel().equals(WARN), "Removing entity association [host]");
  }

  @Test
  void filterLastIdentifyingAttribute() {
    Entity host =
        Entity.builder("host", Attributes.of(stringKey("host.id"), "H1"))
            .setDescription(Attributes.of(stringKey("host.name"), "machine"))
            .build();
    Resource filtered =
        Resource.builder().addEntity(host).removeIf(key -> key.getKey().equals("host.id")).build();
    assertThat(filtered.getEntities()).isEmpty();
    assertThat(filtered.getUnassociatedAttributes()).isEqualTo(host.getDescription());
    assertThat(filtered.getAttributes()).isEqualTo(host.getDescription());
    assertThat(filtered.toBuilder().build()).isEqualTo(filtered);
  }

  @Test
  void noOpEntityOperationsDoNotLogChanges() {
    Entity host =
        Entity.builder("host", Attributes.of(stringKey("host.id"), "H1"))
            .setDescription(Attributes.of(stringKey("host.name"), "machine"))
            .build();
    Resource original = EntityUtil.createResource(singletonList(host));
    assertThat(original.merge(original)).isEqualTo(original);
    assertThat(
            original.toBuilder()
                .removeIf(key -> false)
                .put("host.id", "H1")
                .put("host.name", "machine")
                .build())
        .isEqualTo(original);
    logs.assertDoesNotContain("Updating descriptive attributes");
    logs.assertDoesNotContain("Removing descriptive attribute keys");
    logs.assertDoesNotContain("Removing entity association");
    logs.assertDoesNotContain("Replacing entity");
  }

  @Test
  void filterDescriptionPreservesAssociation() {
    Entity host =
        Entity.builder("host", Attributes.of(stringKey("host.id"), "H1"))
            .setDescription(Attributes.of(stringKey("host.name"), "machine"))
            .build();
    Resource original = Resource.builder().addEntity(host).build();
    Resource filtered =
        original.toBuilder().removeIf(key -> key.getKey().equals("host.name")).build();

    assertThat(filtered.getEntities())
        .singleElement()
        .satisfies(
            entity -> {
              assertThat(entity.getId()).isEqualTo(host.getId());
              assertThat(entity.getDescription()).isEqualTo(Attributes.empty());
            });
    assertThat(filtered.getAttributes()).isEqualTo(host.getId());
    assertThat(filtered.getUnassociatedAttributes()).isEqualTo(Attributes.empty());
    logs.assertContains(
        event -> event.getLevel().equals(WARN),
        "Removing descriptive attribute keys from entity [host]");
  }

  @Test
  void schemaCopyingDiffersFromDerivationAndMerging() {
    Entity host =
        Entity.builder("host", Attributes.of(stringKey("host.id"), "H1"))
            .setSchemaUrl("S1")
            .build();
    Entity process =
        Entity.builder("process", Attributes.of(longKey("process.pid"), 1L))
            .setSchemaUrl("S2")
            .build();
    ResourceBuilder builder = Resource.builder().addEntity(host);
    Resource hostResource = builder.build();
    Resource processResource = EntityUtil.createResource(singletonList(process));

    assertThat(builder.addEntity(process).build().getSchemaUrl()).isNull();
    assertThat(hostResource.toBuilder().addEntity(process).build().getSchemaUrl()).isEqualTo("S1");
    assertThat(hostResource.merge(processResource).getSchemaUrl()).isNull();
    assertThat(
            Resource.builder().putAll(hostResource).putAll(processResource).build().getSchemaUrl())
        .isNull();
    assertThat(
            Resource.builder()
                .setSchemaUrl("configured")
                .putAll(hostResource)
                .putAll(processResource)
                .build()
                .getSchemaUrl())
        .isEqualTo("configured");
  }

  @Test
  void mergeGroupingCanChangeAssociations() {
    Entity host =
        Entity.builder("host", Attributes.of(stringKey("host.id"), "H1"))
            .setDescription(Attributes.of(stringKey("host.name"), "old"))
            .build();
    Resource a = EntityUtil.createResource(singletonList(host));
    Resource b = Resource.create(Attributes.of(stringKey("host.name"), "new"));
    Resource c =
        EntityUtil.createResource(singletonList(Entity.builder("host", host.getId()).build()));
    Resource left = a.merge(b).merge(c);
    Resource right = a.merge(b.merge(c));

    assertThat(left.getAttributes()).isEqualTo(right.getAttributes());
    assertThat(left.getEntities()).hasSize(1);
    assertThat(right.getEntities()).isEmpty();
  }

  @Test
  void valueEqualityIncludesEntityOrderAndDescriptions() {
    Entity host = Entity.builder("host", Attributes.of(stringKey("host.id"), "H1")).build();
    Entity process = Entity.builder("process", Attributes.of(longKey("process.pid"), 1L)).build();
    Resource original = EntityUtil.createResource(Arrays.asList(host, process));
    Resource reordered = original.merge(EntityUtil.createResource(singletonList(host)));
    Resource described =
        original.merge(
            EntityUtil.createResource(
                singletonList(
                    host.toBuilder()
                        .setDescription(Attributes.of(stringKey("host.name"), "machine"))
                        .build())));

    assertThat(reordered.getAttributes()).isEqualTo(original.getAttributes());
    assertThat(reordered.getEntities()).containsExactlyInAnyOrderElementsOf(original.getEntities());
    assertThat(reordered).isNotEqualTo(original);
    assertThat(described).isNotEqualTo(original);
    assertThat(described.getAttribute(stringKey("host.id"))).isEqualTo("H1");
  }

  @Test
  void equalityIncludesDescriptionClassification() {
    Entity host = Entity.builder("host", Attributes.of(stringKey("host.id"), "H1")).build();
    Resource ordinaryName = Resource.builder().addEntity(host).put("host.name", "machine").build();
    Resource describedName =
        Resource.builder()
            .addEntity(
                host.toBuilder()
                    .setDescription(Attributes.of(stringKey("host.name"), "machine"))
                    .build())
            .build();
    assertThat(ordinaryName.getAttributes()).isEqualTo(describedName.getAttributes());
    assertThat(ordinaryName.getSchemaUrl()).isEqualTo(describedName.getSchemaUrl());
    assertThat(ordinaryName).isNotEqualTo(describedName);
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
    assertThat(resource.getAttribute(stringKey("service.name"))).isEqualTo("unknown_service:java");
    assertThat(resource.getAttribute(stringKey("telemetry.sdk.name"))).isEqualTo("opentelemetry");
    assertThat(resource.getAttribute(stringKey("telemetry.sdk.language"))).isEqualTo("java");
    assertThat(resource.getAttribute(stringKey("telemetry.sdk.version")))
        .isEqualTo(System.getProperty("otel.test.project-version"));
    assertThat(resource.getEntities())
        .containsExactly(
            Entity.builder(
                    "telemetry.sdk",
                    Attributes.of(
                        stringKey("telemetry.sdk.name"),
                        "opentelemetry",
                        stringKey("telemetry.sdk.language"),
                        "java"))
                .setDescription(
                    Attributes.of(
                        stringKey("telemetry.sdk.version"),
                        System.getProperty("otel.test.project-version")))
                .setSchemaUrl("https://opentelemetry.io/schemas/1.40.0")
                .build());
    assertThat(resource.getUnassociatedAttributes())
        .isEqualTo(Attributes.of(stringKey("service.name"), "unknown_service:java"));
    assertThat(resource.getSchemaUrl()).isNull();
    Resource rebuilt = resource.toBuilder().build();
    assertThat(rebuilt.getSchemaUrl()).isEqualTo("https://opentelemetry.io/schemas/1.40.0");
    assertThat(rebuilt.getAttributes()).isEqualTo(resource.getAttributes());
    assertThat(rebuilt.getEntities()).containsExactlyElementsOf(resource.getEntities());
    assertThat(Resource.empty().merge(resource)).isEqualTo(resource);
  }

  @ParameterizedTest
  @MethodSource("defaultTelemetrySdkOverrides")
  void testDefaultResources_telemetrySdkOverride(String key, String value) {
    Resource resource =
        Resource.getDefault().merge(Resource.create(Attributes.of(stringKey(key), value)));

    assertThat(resource.getEntities()).isEmpty();
    assertThat(resource.getAttributes())
        .isEqualTo(Resource.getDefault().getAttributes().toBuilder().put(key, value).build());
    assertThat(resource.getUnassociatedAttributes()).isEqualTo(resource.getAttributes());
  }

  static Stream<Arguments> defaultTelemetrySdkOverrides() {
    return Stream.of(
        Arguments.argumentSet("name", "telemetry.sdk.name", "custom-sdk"),
        Arguments.argumentSet("language", "telemetry.sdk.language", "kotlin"),
        Arguments.argumentSet("version", "telemetry.sdk.version", "custom-version"));
  }

  @ParameterizedTest
  @MethodSource("defaultServiceOverrides")
  void testDefaultResources_serviceOverride(Resource override, Resource expected) {
    assertThat(Resource.getDefault().merge(override)).isEqualTo(expected);
    assertThat(Resource.getDefault().toBuilder().putAll(override).build())
        .isEqualTo(expected.toBuilder().build());
    assertThat(logs.getEvents()).filteredOn(event -> event.getLevel().equals(WARN)).isEmpty();
  }

  static Stream<Arguments> defaultServiceOverrides() {
    Resource defaults = Resource.getDefault();
    Entity service =
        Entity.builder("service", Attributes.of(stringKey("service.name"), "configured"))
            .setSchemaUrl("https://opentelemetry.io/schemas/1.40.0")
            .build();
    Resource configuredService = Resource.builder().addEntity(service).build();
    Resource withoutService =
        defaults.toBuilder()
            .removeIf(key -> key.getKey().equals("service.name"))
            .buildWithSchemaUrl(null);
    return Stream.of(
        Arguments.argumentSet(
            "unchanged name preserves fallback",
            Resource.create(Attributes.of(stringKey("service.name"), "unknown_service:java")),
            defaults),
        Arguments.argumentSet(
            "ordinary name overrides fallback",
            Resource.create(Attributes.of(stringKey("service.name"), "configured")),
            withoutService.toBuilder().put("service.name", "configured").buildWithSchemaUrl(null)),
        Arguments.argumentSet(
            "entity replaces fallback",
            configuredService,
            withoutService.toBuilder().addEntity(service).build()));
  }

  @Test
  void shouldBuilderNotFailWithNullResource() {
    // given
    ResourceBuilder builder = Resource.getDefault().toBuilder();

    // when
    builder.putAll((Resource) null);

    // then no exception is thrown
    // and
    assertThat(builder.build().getAttribute(stringKey("service.name")))
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

  @Test
  public void toBuilder() {

    Resource resource =
        Resource.builder().setSchemaUrl("http://example.com").put("foo", "val").build();

    Resource newResource = resource.toBuilder().build();

    assertThat(newResource).isNotSameAs(Resource.getDefault());
    assertThat(newResource.getAttribute(stringKey("foo"))).isEqualTo("val");
    assertThat(newResource.getSchemaUrl()).isEqualTo("http://example.com");
  }

  @Test
  public void removeIf() {
    assertThat(Resource.builder().removeIf(unused -> true).build()).isEqualTo(Resource.empty());
    assertThat(Resource.builder().removeIf(key -> key.getKey().equals("key1")).build())
        .isEqualTo(Resource.empty());
    assertThat(
            Resource.builder()
                .put("key1", "value1")
                .removeIf(key -> key.getKey().equals("key1"))
                .removeIf(key -> key.getKey().equals("key1"))
                .build())
        .isEqualTo(Resource.empty());
    assertThat(
            Resource.builder()
                .put("key1", "value1")
                .put("key1", "value2")
                .put("key2", "value2")
                .put("key3", "value3")
                .removeIf(key -> key.getKey().equals("key1"))
                .build())
        .isEqualTo(Resource.builder().put("key2", "value2").put("key3", "value3").build());
    assertThat(
            Resource.builder()
                .put("key1", "value1A")
                .put("key1", true)
                .removeIf(
                    key ->
                        key.getKey().equals("key1") && key.getType().equals(AttributeType.STRING))
                .build())
        .isEqualTo(Resource.builder().put("key1", true).build());
    assertThat(
            Resource.builder()
                .put("key1", "value1")
                .put("key2", "value2")
                .put("foo", "bar")
                .removeIf(key -> key.getKey().matches("key.*"))
                .build())
        .isEqualTo(Resource.builder().put("foo", "bar").build());
  }
}
