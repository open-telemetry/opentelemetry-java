/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Exercises the limits-enforcing builder returned by {@link Attributes#builder(AttributeLimits)}.
 */
class BoundedAttributesBuilderTest {

  @Test
  void noLimits_equivalentToDefaultBuilder() {
    Attributes attrs =
        Attributes.builder(AttributeLimits.noLimits())
            .put(stringKey("k"), "v")
            .put(longKey("n"), 1L)
            .build();
    assertThat(attrs.size()).isEqualTo(2);
  }

  @Test
  void sameNameDifferentType_lastValueWins() {
    Attributes attrs =
        Attributes.builder(AttributeLimits.builder().setCapacity(10).build())
            .put(stringKey("k"), "hello")
            .put(booleanKey("k"), true)
            .build();
    assertThat(attrs.size()).isEqualTo(1);
    assertThat(attrs.get(booleanKey("k"))).isEqualTo(true);
    assertThat(attrs.get(stringKey("k"))).isNull();
  }

  @Test
  void sameNameDifferentType_doesNotConsumeExtraCapacity() {
    Attributes attrs =
        Attributes.builder(AttributeLimits.builder().setCapacity(2).build())
            .put(stringKey("a"), "v1")
            .put(booleanKey("a"), false)
            .put(longKey("b"), 42L)
            .build();
    assertThat(attrs.size()).isEqualTo(2);
    assertThat(attrs.get(booleanKey("a"))).isEqualTo(false);
    assertThat(attrs.get(longKey("b"))).isEqualTo(42L);
  }

  @Test
  void capacityDropsOverflow() {
    Attributes attrs =
        Attributes.builder(AttributeLimits.builder().setCapacity(2).build())
            .put(stringKey("a"), "v1")
            .put(stringKey("b"), "v2")
            .put(stringKey("c"), "v3")
            .build();
    assertThat(attrs.size()).isEqualTo(2);
    assertThat(attrs.get(stringKey("c"))).isNull();
  }

  @Test
  void lengthLimitTruncatesStringValues() {
    Attributes attrs =
        Attributes.builder(AttributeLimits.builder().setLengthLimit(3).build())
            .put(stringKey("k"), "hello")
            .build();
    assertThat(attrs.get(stringKey("k"))).isEqualTo("hel");
  }

  @Test
  void buildIsCached_returnsSameInstanceBetweenMutations() {
    AttributesBuilder builder =
        Attributes.builder(AttributeLimits.builder().setCapacity(10).build())
            .put(stringKey("a"), "v");
    Attributes first = builder.build();
    Attributes second = builder.build();
    assertThat(second).isSameAs(first);
    builder.put(stringKey("b"), "v2");
    Attributes third = builder.build();
    assertThat(third).isNotSameAs(first);
    assertThat(third.size()).isEqualTo(2);
  }

  @Test
  void remove_invalidatesCache() {
    AttributesBuilder builder =
        Attributes.builder(AttributeLimits.builder().setCapacity(10).build())
            .put(stringKey("a"), "v1")
            .put(stringKey("b"), "v2");
    Attributes first = builder.build();
    builder.remove(stringKey("a"));
    Attributes second = builder.build();
    assertThat(second).isNotSameAs(first);
    assertThat(second.size()).isEqualTo(1);
  }
}
