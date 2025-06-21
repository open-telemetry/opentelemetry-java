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
import org.junit.jupiter.api.Test;

/** Unit tests for {@link EntityUtil}. */
class EntityUtilTest {
  @Test
  void testMerge_entities_same_types_and_id() {
    Collection<Entity> base =
        Arrays.asList(
            Entity.builder("a")
                .setSchemaUrl("one")
                .withId(id -> id.put("a.id", "a"))
                .withDescription(builder -> builder.put("a.desc1", "a"))
                .build());
    Collection<Entity> added =
        Arrays.asList(
            Entity.builder("a")
                .setSchemaUrl("one")
                .withId(
                    builder -> {
                      builder.put("a.id", "a");
                    })
                .withDescription(
                    builder -> {
                      builder.put("a.desc2", "b");
                    })
                .build());
    Collection<Entity> merged = EntityUtil.mergeEntities(base, added);
    assertThat(merged).hasSize(1);
    assertThat(merged)
        .anySatisfy(
            entity ->
                assertThat(entity)
                    .hasType("a")
                    .hasSchemaUrl("one")
                    .hasIdSatisfying(id -> assertThat(id).containsEntry("a.id", "a"))
                    .hasDescriptionSatisfying(
                        desc ->
                            assertThat(desc)
                                .containsEntry("a.desc1", "a")
                                .containsEntry("a.desc2", "b")));
  }

  @Test
  void testMerge_entities_same_types_and_id_different_schema() {
    Collection<Entity> base =
        Arrays.asList(
            Entity.builder("a")
                .setSchemaUrl("one")
                .withId(id -> id.put("a.id", "a"))
                .withDescription(builder -> builder.put("a.desc1", "a"))
                .build());
    Collection<Entity> added =
        Arrays.asList(
            Entity.builder("a")
                .setSchemaUrl("two")
                .withId(
                    builder -> {
                      builder.put("a.id", "a");
                    })
                .withDescription(
                    builder -> {
                      builder.put("a.desc2", "b");
                    })
                .build());
    Collection<Entity> merged = EntityUtil.mergeEntities(base, added);
    assertThat(merged).hasSize(1);
    assertThat(merged)
        .anySatisfy(
            entity ->
                assertThat(entity)
                    .hasType("a")
                    .hasSchemaUrl("one")
                    .hasIdSatisfying(id -> assertThat(id).containsEntry("a.id", "a"))
                    .hasDescriptionSatisfying(
                        desc ->
                            assertThat(desc)
                                .containsEntry("a.desc1", "a")
                                // Don't merge between versions.
                                .doesNotContainKey("a.desc2")));
  }

  @Test
  void testMerge_entities_same_types_different_id() {
    Collection<Entity> base =
        Arrays.asList(
            Entity.builder("a")
                .setSchemaUrl("one")
                .withId(
                    builder -> {
                      builder.put("a.id", "a");
                    })
                .withDescription(
                    builder -> {
                      builder.put("a.desc1", "a");
                    })
                .build());
    Collection<Entity> added =
        Arrays.asList(
            Entity.builder("a")
                .setSchemaUrl("one")
                .withId(
                    builder -> {
                      builder.put("a.id", "b");
                    })
                .withDescription(
                    builder -> {
                      builder.put("a.desc2", "b");
                    })
                .build());
    Collection<Entity> merged = EntityUtil.mergeEntities(base, added);
    assertThat(merged).hasSize(1);
    assertThat(merged)
        .satisfiesExactly(
            e ->
                assertThat(e)
                    .hasSchemaUrl("one")
                    .hasIdSatisfying(id -> assertThat(id).containsEntry("a.id", "a"))
                    .hasDescriptionSatisfying(
                        desc ->
                            assertThat(desc)
                                .containsEntry("a.desc1", "a")
                                .doesNotContainKey("a.desc2")));
  }

  @Test
  void testMerge_entities_separate_types_and_schema() {
    Collection<Entity> base =
        Arrays.asList(
            Entity.builder("a")
                .setSchemaUrl("one")
                .withId(
                    builder -> {
                      builder.put("a.id", "a");
                    })
                .build());
    Collection<Entity> added =
        Arrays.asList(
            Entity.builder("b")
                .setSchemaUrl("two")
                .withId(
                    builder -> {
                      builder.put("b.id", "b");
                    })
                .build());
    Collection<Entity> merged = EntityUtil.mergeEntities(base, added);
    // Make sure we keep both entities when no conflict.
    assertThat(merged)
        .satisfiesExactlyInAnyOrder(
            a -> assertThat(a).hasType("a"), b -> assertThat(b).hasType("b"));
  }

  @Test
  void testSchemaUrlMerge_no_entities_differentUrls() {
    // If the we find conflicting schema URLs in resource we must drop schema url (set to null).
    String result = EntityUtil.mergeResourceSchemaUrl(Collections.emptyList(), "one", "two");
    assertThat(result).isNull();
  }

  @Test
  void testSchemaUrlMerge_no_entities_base_null() {
    // If the our resource had no schema url it abides by, we use the incoming schema url.
    String result = EntityUtil.mergeResourceSchemaUrl(Collections.emptyList(), null, "two");
    assertThat(result).isEqualTo("two");
  }

  @Test
  void testSchemaUrlMerge_no_entities_next_null() {
    // If the new resource had no schema url it abides by, we preserve ours.
    // NOTE: this is by specification, but seems problematic if conflicts in merge
    // cause violation of SchemaURL.
    String result = EntityUtil.mergeResourceSchemaUrl(Collections.emptyList(), "one", null);
    assertThat(result).isEqualTo("one");
  }

  @Test
  void testSchemaUrlMerge_entities_same_url() {
    // If the new resource had no schema url it abides by, we preserve ours.
    // NOTE: this is by specification, but seems problematic if conflicts in merge
    // cause violation of SchemaURL.
    String result =
        EntityUtil.mergeResourceSchemaUrl(
            Arrays.asList(
                Entity.builder("t").setSchemaUrl("one").withId(id -> id.put("id", 1)).build()),
            "one",
            null);
    assertThat(result).isEqualTo("one");
  }

  @Test
  void testSchemaUrlMerge_entities_different_url() {
    // When entities have conflicting schema urls, we cannot fill out resource schema url,
    // no matter what.
    String result =
        EntityUtil.mergeResourceSchemaUrl(
            Arrays.asList(
                Entity.builder("t").setSchemaUrl("one").withId(id -> id.put("id", 1)).build(),
                Entity.builder("t2").setSchemaUrl("two").withId(id -> id.put("id2", 1)).build()),
            "one",
            "one");
    assertThat(result).isEqualTo(null);
  }

  @Test
  void testRawAttributeMerge_no_entities() {
    // When no entities are present all attributes are merged.
    RawAttributeMergeResult result =
        EntityUtil.mergeRawAttributes(
            Attributes.builder().put("a", 1).put("b", 1).build(),
            Attributes.builder().put("b", 2).put("c", 2).build(),
            Collections.emptyList());
    assertThat(result.getConflicts()).isEmpty();
    assertThat(result.getAttributes())
        .hasSize(3)
        .containsEntry("a", 1)
        .containsEntry("b", 2)
        .containsEntry("c", 2);
  }

  @Test
  void testRawAttributeMerge_entity_with_conflict() {
    // When an entity conflicts with incoming raw attributes, we need to call out that conflict
    // so resource merge logic can remove the entity from resource.
    RawAttributeMergeResult result =
        EntityUtil.mergeRawAttributes(
            Attributes.builder().put("a", 1).put("b", 1).build(),
            Attributes.builder().put("b", 2).put("c", 2).build(),
            Arrays.asList(Entity.builder("c").withId(id -> id.put("c", 1)).build()));
    assertThat(result.getConflicts()).satisfiesExactly(e -> assertThat(e).hasType("c"));
    assertThat(result.getAttributes())
        .hasSize(3)
        .containsEntry("a", 1)
        .containsEntry("b", 2)
        .containsEntry("c", 2);
  }

  @Test
  void testAddEntity_reflection() {
    Resource result =
        EntityUtil.addEntity(
                Resource.builder(), Entity.builder("a").withId(id -> id.put("a", 1)).build())
            .build();
    assertThat(EntityUtil.getEntities(result))
        .satisfiesExactlyInAnyOrder(e -> assertThat(e).hasType("a"));
  }

  @Test
  void testAddAllEntity_reflection() {
    Resource result =
        EntityUtil.addAllEntity(
                Resource.builder(),
                Arrays.asList(
                    Entity.builder("a").withId(id -> id.put("a", 1)).build(),
                    Entity.builder("b").withId(id -> id.put("b", 1)).build()))
            .build();
    assertThat(EntityUtil.getEntities(result))
        .satisfiesExactlyInAnyOrder(
            e -> assertThat(e).hasType("a"), e -> assertThat(e).hasType("b"));
  }
}
