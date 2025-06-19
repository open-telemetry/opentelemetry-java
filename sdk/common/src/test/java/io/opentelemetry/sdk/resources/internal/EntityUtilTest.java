/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link EntityUtil} */
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
}
