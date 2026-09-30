/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests for {@link EntityUtil}. */
class EntityUtilTest {

  @ParameterizedTest
  @MethodSource("mergeEntitiesTestCases")
  void mergeEntities(
      Collection<Entity> base, Collection<Entity> added, Collection<Entity> expected) {
    assertThat(EntityUtil.mergeEntities(base, added)).containsExactlyInAnyOrderElementsOf(expected);
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
            "same type and id different schema - don't merge between schema versions",
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
                    .setSchemaUrl("one")
                    .setDescription(Attributes.builder().put("a.desc1", "a").build())
                    .build())),
        Arguments.argumentSet(
            "same type different id - don't merge between different identities",
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
                Entity.builder("a", Attributes.builder().put("a.id", "a").build())
                    .setSchemaUrl("one")
                    .setDescription(Attributes.builder().put("a.desc1", "a").build())
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
