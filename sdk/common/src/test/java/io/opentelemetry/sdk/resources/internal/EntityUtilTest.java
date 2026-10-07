/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.slf4j.event.Level.WARN;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests for {@link EntityUtil}. */
class EntityUtilTest {

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(ResourceBuilder.class, WARN);

  @ParameterizedTest
  @MethodSource("mergeEntitiesTestCases")
  void mergeEntities(
      Collection<Entity> base,
      Collection<Entity> added,
      Collection<Entity> expected,
      @Nullable String expectedSchemaUrl) {
    Resource merged = EntityUtil.createResource(base).merge(EntityUtil.createResource(added));
    assertThat(EntityUtil.getEntities(merged)).containsExactlyElementsOf(expected);
    assertThat(EntityUtil.getUnassociatedAttributes(merged)).isEmpty();
    assertThat(merged.getSchemaUrl()).isEqualTo(expectedSchemaUrl);
  }

  static Stream<Arguments> mergeEntitiesTestCases() {
    Entity withoutSchema =
        Entity.builder("a", Attributes.builder().put("a.id", "a").build())
            .setDescription(Attributes.builder().put("a.desc1", "a").build())
            .build();
    Entity withSchema =
        Entity.builder("a", Attributes.builder().put("a.id", "a").build())
            .setSchemaUrl("one")
            .setDescription(Attributes.builder().put("a.desc2", "b").build())
            .build();
    return Stream.of(
        Arguments.argumentSet(
            "same type and id different schema - incoming entity replaces old entity",
            Collections.singletonList(withSchema),
            Collections.singletonList(withSchema.toBuilder().setSchemaUrl("two").build()),
            Collections.singletonList(withSchema.toBuilder().setSchemaUrl("two").build()),
            null),
        Arguments.argumentSet(
            "absent schema replaced by present schema",
            Collections.singletonList(withoutSchema),
            Collections.singletonList(withSchema),
            Collections.singletonList(withSchema),
            "one"),
        Arguments.argumentSet(
            "present schema replaced by absent schema",
            Collections.singletonList(withSchema),
            Collections.singletonList(withoutSchema),
            Collections.singletonList(withoutSchema),
            "one"),
        Arguments.argumentSet(
            "same type different id - incoming entity replaces old entity",
            Collections.singletonList(withSchema),
            Collections.singletonList(
                Entity.builder("a", Attributes.builder().put("a.id", "b").build())
                    .setSchemaUrl("one")
                    .build()),
            Collections.singletonList(
                Entity.builder("a", Attributes.builder().put("a.id", "b").build())
                    .setSchemaUrl("one")
                    .build()),
            "one"),
        Arguments.argumentSet(
            "separate types and schema - both entities are kept",
            Collections.singletonList(
                Entity.builder("a", Attributes.builder().put("a.id", "a").build())
                    .setSchemaUrl("one")
                    .build()),
            Collections.singletonList(
                Entity.builder("b", Attributes.builder().put("b.id", "b").build())
                    .setSchemaUrl("two")
                    .build()),
            Arrays.asList(
                Entity.builder("a", Attributes.builder().put("a.id", "a").build())
                    .setSchemaUrl("one")
                    .build(),
                Entity.builder("b", Attributes.builder().put("b.id", "b").build())
                    .setSchemaUrl("two")
                    .build()),
            null));
  }

  @ParameterizedTest
  @MethodSource("mergeDescriptionTestCases")
  void mergeDescriptions(@Nullable String schemaUrl) {
    Entity base =
        Entity.builder("host", Attributes.builder().put("host.id", "H1").build())
            .setDescription(
                Attributes.builder().put("host.name", "old").put("host.arch", "arm64").build())
            .build();
    Entity incoming =
        Entity.builder("host", base.getId())
            .setDescription(
                Attributes.builder().put("host.name", "new").put("host.type", "vm").build())
            .build();
    if (schemaUrl != null) {
      base = base.toBuilder().setSchemaUrl(schemaUrl).build();
      incoming = incoming.toBuilder().setSchemaUrl(schemaUrl).build();
    }

    Resource merged =
        EntityUtil.createResource(Collections.singletonList(base))
            .merge(EntityUtil.createResource(Collections.singletonList(incoming)));
    Entity expected =
        incoming.toBuilder()
            .setDescription(
                Attributes.builder()
                    .put("host.name", "new")
                    .put("host.arch", "arm64")
                    .put("host.type", "vm")
                    .build())
            .build();

    assertThat(merged).isEqualTo(EntityUtil.createResource(Collections.singletonList(expected)));
    logs.assertContains(
        event -> event.getLevel().equals(WARN), "Updating descriptive attributes of entity [host]");
  }

  static Stream<Arguments> mergeDescriptionTestCases() {
    return Stream.of(
        Arguments.argumentSet("matching schemas", "https://opentelemetry.io/schemas/1.40.0"),
        Arguments.argumentSet("both schemas absent", (String) null));
  }

  @ParameterizedTest
  @MethodSource("absentSchemaRepresentations")
  void mergeAbsentSchemaRepresentations(
      @Nullable String baseSchema, @Nullable String incomingSchema) {
    Attributes id = Attributes.of(AttributeKey.stringKey("host.id"), "H1");
    Entity base =
        spy(
            SdkEntity.create(
                "host",
                id,
                Attributes.of(AttributeKey.stringKey("host.arch"), "arm64"),
                baseSchema));
    Entity incoming =
        spy(
            SdkEntity.create(
                "host",
                id,
                Attributes.of(AttributeKey.stringKey("host.name"), "new"),
                incomingSchema));
    doReturn(baseSchema).when(base).getSchemaUrl();
    doReturn(incomingSchema).when(incoming).getSchemaUrl();

    Resource merged =
        EntityUtil.createResource(Collections.singletonList(base))
            .merge(EntityUtil.createResource(Collections.singletonList(incoming)));

    assertThat(EntityUtil.getEntities(merged))
        .singleElement()
        .satisfies(
            entity -> {
              assertThat(entity.getId()).isEqualTo(id);
              assertThat(entity.getDescription())
                  .isEqualTo(
                      Attributes.builder()
                          .put("host.arch", "arm64")
                          .put("host.name", "new")
                          .build());
              assertThat(entity.getSchemaUrl()).isNull();
            });
    assertThat(merged.getSchemaUrl()).isNull();
    logs.assertDoesNotContain("Replacing entity");
  }

  static Stream<Arguments> absentSchemaRepresentations() {
    return Stream.of(
        Arguments.argumentSet("null / null", null, null),
        Arguments.argumentSet("empty / empty", "", ""),
        Arguments.argumentSet("null / empty", null, ""),
        Arguments.argumentSet("empty / null", "", null));
  }

  @ParameterizedTest
  @MethodSource("mergeCrossEntityConflictTestCases")
  void mergeCrossEntityConflict(
      boolean existingIdentifying,
      boolean incomingIdentifying,
      Attributes existingConflict,
      Attributes incomingConflict) {
    Entity existing =
        Entity.builder(
                "existing",
                Attributes.builder()
                    .put("existing.id", "E1")
                    .putAll(existingIdentifying ? existingConflict : Attributes.empty())
                    .build())
            .setDescription(
                Attributes.builder()
                    .put("existing.description", "preserved")
                    .putAll(existingIdentifying ? Attributes.empty() : existingConflict)
                    .build())
            .build();
    Entity incoming =
        Entity.builder(
                "incoming",
                Attributes.builder()
                    .put("incoming.id", "I1")
                    .putAll(incomingIdentifying ? incomingConflict : Attributes.empty())
                    .build())
            .setDescription(incomingIdentifying ? Attributes.empty() : incomingConflict)
            .build();
    Entity unrelated =
        Entity.builder("unrelated", Attributes.builder().put("other.id", "O1").build()).build();
    ResourceBuilder baseBuilder = Resource.builder().put("raw", "preserved");
    EntityUtil.addEntity(baseBuilder, existing);
    EntityUtil.addEntity(baseBuilder, unrelated);

    Resource merged =
        baseBuilder.build().merge(EntityUtil.createResource(Collections.singletonList(incoming)));

    assertThat(EntityUtil.getEntities(merged)).containsExactly(unrelated, incoming);
    assertThat(EntityUtil.getUnassociatedAttributes(merged))
        .isEqualTo(
            Attributes.builder()
                .put("raw", "preserved")
                .put("existing.id", "E1")
                .put("existing.description", "preserved")
                .build());
    assertThat(merged.getAttributes())
        .isEqualTo(
            Attributes.builder()
                .putAll(EntityUtil.getUnassociatedAttributes(merged))
                .putAll(unrelated.getId())
                .putAll(incoming.getId())
                .putAll(incoming.getDescription())
                .build());
    logs.assertContains(
        event -> event.getLevel().equals(WARN), "Removing entity association [existing]");
  }

  static Stream<Arguments> mergeCrossEntityConflictTestCases() {
    Attributes old = Attributes.of(AttributeKey.stringKey("shared"), "old");
    Attributes updated = Attributes.of(AttributeKey.stringKey("shared"), "new");
    return Stream.of(
        Arguments.argumentSet("incoming id overwrites existing id", true, true, old, updated),
        Arguments.argumentSet(
            "incoming description overwrites existing id", true, false, old, updated),
        Arguments.argumentSet(
            "incoming id overwrites existing description", false, true, old, updated),
        Arguments.argumentSet(
            "incoming description overwrites existing description", false, false, old, updated),
        Arguments.argumentSet(
            "equal values still transfer attribute key ownership", false, false, old, old),
        Arguments.argumentSet(
            "different attribute types",
            true,
            true,
            Attributes.of(AttributeKey.longKey("shared"), 1L),
            updated));
  }

  @Test
  void mergeReplacementAndCrossEntityConflict() {
    Entity oldHost =
        Entity.builder("host", Attributes.builder().put("host.id", "H2").build())
            .setDescription(Attributes.builder().put("host.name", "discarded").build())
            .build();
    Entity service =
        Entity.builder("service", Attributes.builder().put("service.name", "S1").build())
            .setDescription(Attributes.builder().put("env", "dev").build())
            .build();
    Entity incoming =
        Entity.builder("host", Attributes.builder().put("host.id", "H1").build())
            .setDescription(Attributes.builder().put("env", "prod").build())
            .build();

    Resource merged =
        EntityUtil.createResource(Arrays.asList(oldHost, service))
            .merge(EntityUtil.createResource(Collections.singletonList(incoming)));

    assertThat(EntityUtil.getEntities(merged)).containsExactly(incoming);
    assertThat(merged.getAttributes())
        .isEqualTo(
            Attributes.builder()
                .put("host.id", "H1")
                .put("env", "prod")
                .put("service.name", "S1")
                .build());
    assertThat(EntityUtil.getUnassociatedAttributes(merged))
        .isEqualTo(Attributes.of(AttributeKey.stringKey("service.name"), "S1"));
    logs.assertContains(
        event -> event.getLevel().equals(WARN),
        "Replacing entity [host] because identifying attributes differ.");
    logs.assertContains(
        event -> event.getLevel().equals(WARN),
        "Removing entity association [service] because incoming entity [host] overwrites its attributes. Non-conflicting attributes are retained as unassociated.");
  }

  @Test
  void mergeIncomingEntitiesInOrder() {
    Entity host =
        Entity.builder("host", Attributes.builder().put("host.id", "H1").build())
            .setDescription(Attributes.builder().put("env", "host-env").build())
            .build();
    Entity service =
        Entity.builder("service", Attributes.builder().put("service.name", "S1").build())
            .setDescription(Attributes.builder().put("env", "service-env").build())
            .build();

    Resource hostThenService =
        Resource.empty().merge(EntityUtil.createResource(Arrays.asList(host, service)));
    Resource serviceThenHost =
        Resource.empty().merge(EntityUtil.createResource(Arrays.asList(service, host)));

    assertThat(EntityUtil.getEntities(hostThenService)).containsExactly(service);
    assertThat(EntityUtil.getUnassociatedAttributes(hostThenService))
        .isEqualTo(Attributes.of(AttributeKey.stringKey("host.id"), "H1"));
    assertThat(hostThenService.getAttributes()).containsEntry("env", "service-env");
    assertThat(EntityUtil.getEntities(serviceThenHost)).containsExactly(host);
    assertThat(EntityUtil.getUnassociatedAttributes(serviceThenHost))
        .isEqualTo(Attributes.of(AttributeKey.stringKey("service.name"), "S1"));
    assertThat(serviceThenHost.getAttributes()).containsEntry("env", "host-env");
  }

  @ParameterizedTest
  @MethodSource("attributeTypeChangeCases")
  void attributeTypeChangeRemovesAssociation(Attributes before, Attributes after) {
    Entity entity = Entity.builder("test", before).build();
    Resource original = EntityUtil.createResource(Collections.singletonList(entity));
    Resource merged = original.merge(Resource.create(after));
    assertThat(EntityUtil.getEntities(merged)).isEmpty();
    assertThat(merged.getAttributes()).isEqualTo(after);
    assertThat(original.toBuilder().putAll(after).build()).isEqualTo(merged);
    logs.assertContains(
        event -> event.getLevel().equals(WARN), "Removing entity association [test]");
  }

  static Stream<Arguments> attributeTypeChangeCases() {
    return Stream.of(
        Arguments.argumentSet(
            "equal-looking numbers",
            Attributes.of(AttributeKey.longKey("key"), 1L),
            Attributes.of(AttributeKey.doubleKey("key"), 1.0)),
        Arguments.argumentSet(
            "equal empty lists",
            Attributes.of(AttributeKey.longArrayKey("key"), Collections.emptyList()),
            Attributes.of(AttributeKey.stringArrayKey("key"), Collections.emptyList())));
  }

  @Test
  void duplicateEntityReferencesAreNormalized() {
    Entity first = Entity.builder("test", Attributes.of(AttributeKey.stringKey("id"), "1")).build();
    Entity second =
        Entity.builder("test", Attributes.of(AttributeKey.stringKey("id"), "2")).build();
    Resource resource = EntityUtil.createResource(Arrays.asList(first, first, second));
    assertThat(EntityUtil.getEntities(resource)).containsExactly(second);
    assertThat(resource.getAttributes()).isEqualTo(second.getId());
  }

  @Test
  void addEntity_reflection() {
    Resource result =
        EntityUtil.addEntity(
                Resource.builder(),
                Entity.builder("a", Attributes.builder().put("a", 1).build()).build())
            .build();
    assertThat(EntityUtil.getEntities(result))
        .satisfiesExactlyInAnyOrder(e -> assertThat(e.getType()).isEqualTo("a"));
  }
}
