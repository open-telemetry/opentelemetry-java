/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.event.Level.DEBUG;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
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
  LogCapturer logs = LogCapturer.create().captureForType(EntityUtil.class, DEBUG);

  @ParameterizedTest
  @MethodSource("mergeEntitiesTestCases")
  void mergeEntities(
      Collection<Entity> base, Collection<Entity> added, Collection<Entity> expected) {
    Resource merged = EntityUtil.createResource(base).merge(EntityUtil.createResource(added));
    assertThat(merged).isEqualTo(EntityUtil.createResource(expected));
  }

  static Stream<Arguments> mergeEntitiesTestCases() {
    return Stream.of(
        Arguments.argumentSet(
            "same type and id - descriptions are merged",
            Collections.singletonList(
                Entity.builder("a", Attributes.builder().put("a.id", "a").build())
                    .setSchemaUrl("one")
                    .setDescription(Attributes.builder().put("a.desc1", "a").build())
                    .build()),
            Collections.singletonList(
                Entity.builder("a", Attributes.builder().put("a.id", "a").build())
                    .setSchemaUrl("one")
                    .setDescription(Attributes.builder().put("a.desc2", "b").build())
                    .build()),
            Collections.singletonList(
                Entity.builder("a", Attributes.builder().put("a.id", "a").build())
                    .setSchemaUrl("one")
                    .setDescription(
                        Attributes.builder().put("a.desc1", "a").put("a.desc2", "b").build())
                    .build())),
        Arguments.argumentSet(
            "same type and id different schema - incoming entity replaces old entity",
            Collections.singletonList(
                Entity.builder("a", Attributes.builder().put("a.id", "a").build())
                    .setSchemaUrl("one")
                    .setDescription(Attributes.builder().put("a.desc1", "a").build())
                    .build()),
            Collections.singletonList(
                Entity.builder("a", Attributes.builder().put("a.id", "a").build())
                    .setSchemaUrl("two")
                    .setDescription(Attributes.builder().put("a.desc2", "b").build())
                    .build()),
            Collections.singletonList(
                Entity.builder("a", Attributes.builder().put("a.id", "a").build())
                    .setSchemaUrl("two")
                    .setDescription(Attributes.builder().put("a.desc2", "b").build())
                    .build())),
        Arguments.argumentSet(
            "same type different id - incoming entity replaces old entity",
            Collections.singletonList(
                Entity.builder("a", Attributes.builder().put("a.id", "a").build())
                    .setSchemaUrl("one")
                    .setDescription(Attributes.builder().put("a.desc1", "a").build())
                    .build()),
            Collections.singletonList(
                Entity.builder("a", Attributes.builder().put("a.id", "b").build())
                    .setSchemaUrl("one")
                    .setDescription(Attributes.builder().put("a.desc2", "b").build())
                    .build()),
            Collections.singletonList(
                Entity.builder("a", Attributes.builder().put("a.id", "b").build())
                    .setSchemaUrl("one")
                    .setDescription(Attributes.builder().put("a.desc2", "b").build())
                    .build())),
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
                    .build())));
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
  }

  static Stream<Arguments> mergeDescriptionTestCases() {
    return Stream.of(
        Arguments.argumentSet("matching schemas", "https://opentelemetry.io/schemas/1.40.0"),
        Arguments.argumentSet("both schemas absent", (String) null));
  }

  @ParameterizedTest
  @MethodSource("mergeAbsentSchemaTestCases")
  void mergeAbsentSchema(boolean incomingHasSchema) {
    Entity withoutSchema =
        Entity.builder("host", Attributes.builder().put("host.id", "H1").build())
            .setDescription(Attributes.builder().put("host.name", "without-schema").build())
            .build();
    Entity withSchema =
        Entity.builder("host", withoutSchema.getId())
            .setSchemaUrl("https://opentelemetry.io/schemas/1.40.0")
            .setDescription(Attributes.builder().put("host.type", "vm").build())
            .build();
    Entity base = incomingHasSchema ? withoutSchema : withSchema;
    Entity incoming = incomingHasSchema ? withSchema : withoutSchema;

    Resource merged =
        EntityUtil.createResource(Collections.singletonList(base))
            .merge(EntityUtil.createResource(Collections.singletonList(incoming)));

    assertThat(merged).isEqualTo(EntityUtil.createResource(Collections.singletonList(incoming)));
    logs.assertContains(
        event -> event.getLevel().equals(DEBUG),
        "Replacing entity [host] because schema URLs differ.");
  }

  static Stream<Arguments> mergeAbsentSchemaTestCases() {
    return Stream.of(
        Arguments.argumentSet("absent schema replaced by present schema", true),
        Arguments.argumentSet("present schema replaced by absent schema", false));
  }

  @ParameterizedTest
  @MethodSource("mergeCrossEntityConflictTestCases")
  void mergeCrossEntityConflict(boolean existingIdentifying, boolean incomingIdentifying) {
    Attributes existingConflict = Attributes.of(AttributeKey.stringKey("shared"), "old");
    Attributes incomingConflict = Attributes.of(AttributeKey.stringKey("shared"), "new");
    Attributes existingId =
        Attributes.builder()
            .put("existing.id", "E1")
            .putAll(existingIdentifying ? existingConflict : Attributes.empty())
            .build();
    Attributes existingDescription =
        Attributes.builder()
            .put("existing.description", "preserved")
            .putAll(existingIdentifying ? Attributes.empty() : existingConflict)
            .build();
    Entity existing =
        Entity.builder("existing", existingId).setDescription(existingDescription).build();
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
    Resource base =
        EntityUtil.createResourceRaw(
            Attributes.builder().put("raw", "preserved").build(),
            null,
            Arrays.asList(existing, unrelated));

    Resource merged = base.merge(EntityUtil.createResource(Collections.singletonList(incoming)));

    assertThat(EntityUtil.getEntities(merged)).containsExactlyInAnyOrder(unrelated, incoming);
    assertThat(EntityUtil.getUnassociatedAttributes(merged))
        .isEqualTo(
            Attributes.builder()
                .put("raw", "preserved")
                .put("existing.id", "E1")
                .put("existing.description", "preserved")
                .build());
    assertThat(merged.getAttributes()).containsEntry("shared", "new");
  }

  static Stream<Arguments> mergeCrossEntityConflictTestCases() {
    return Stream.of(
        Arguments.argumentSet("incoming id overwrites existing id", true, true),
        Arguments.argumentSet("incoming description overwrites existing id", true, false),
        Arguments.argumentSet("incoming id overwrites existing description", false, true),
        Arguments.argumentSet(
            "incoming description overwrites existing description", false, false));
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
        event -> event.getLevel().equals(DEBUG),
        "Replacing entity [host] because identifying attributes differ.");
    logs.assertContains(
        event -> event.getLevel().equals(DEBUG),
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
  @MethodSource("mergeResourceSchemaUrlTestCases")
  void mergeResourceSchemaUrl(
      Collection<Entity> entities,
      @Nullable String baseUrl,
      @Nullable String nextUrl,
      @Nullable String expected) {
    assertThat(EntityUtil.mergeResourceSchemaUrl(entities, baseUrl, nextUrl)).isEqualTo(expected);
  }

  static Stream<Arguments> mergeResourceSchemaUrlTestCases() {
    return Stream.of(
        Arguments.argumentSet(
            "no entities conflicting urls - drop schema url",
            Collections.emptyList(),
            "one",
            "two",
            null),
        Arguments.argumentSet(
            "no entities base null - use incoming url",
            Collections.emptyList(),
            null,
            "two",
            "two"),
        Arguments.argumentSet(
            "no entities next null - preserve base url",
            Collections.emptyList(),
            "one",
            null,
            "one"),
        Arguments.argumentSet(
            "entities with same url",
            Collections.singletonList(
                Entity.builder("t", Attributes.builder().put("id", 1).build())
                    .setSchemaUrl("one")
                    .build()),
            "one",
            null,
            "one"),
        Arguments.argumentSet(
            "entities with conflicting urls - cannot fill resource schema url",
            Arrays.asList(
                Entity.builder("t", Attributes.builder().put("id", 1).build())
                    .setSchemaUrl("one")
                    .build(),
                Entity.builder("t2", Attributes.builder().put("id2", 1).build())
                    .setSchemaUrl("two")
                    .build()),
            "one",
            "one",
            null));
  }

  @ParameterizedTest
  @MethodSource("mergeRawAttributesTestCases")
  void mergeRawAttributes(Collection<Entity> entities, Collection<Entity> expectedConflicts) {
    RawAttributeMergeResult result =
        EntityUtil.mergeRawAttributes(
            Attributes.builder().put("a", 1).put("b", 1).build(),
            Attributes.builder().put("b", 2).put("c", 2).build(),
            entities);
    assertThat(result.getConflicts()).containsExactlyInAnyOrderElementsOf(expectedConflicts);
    assertThat(result.getAttributes())
        .hasSize(3)
        .containsEntry("a", 1)
        .containsEntry("b", 2)
        .containsEntry("c", 2);
  }

  static Stream<Arguments> mergeRawAttributesTestCases() {
    return Stream.of(
        Arguments.argumentSet(
            "no entities - all attributes merged",
            Collections.emptyList(),
            Collections.emptyList()),
        Arguments.argumentSet(
            "entity id attribute conflicts with incoming attribute",
            Collections.singletonList(
                Entity.builder("c", Attributes.builder().put("c", 1).build()).build()),
            Collections.singletonList(
                Entity.builder("c", Attributes.builder().put("c", 1).build()).build())));
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
