/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.common;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.Test;

class ContextAttributesTest {

  @Test
  void emptyContext_returnsEmptyAttributes() {
    assertThat(ContextAttributes.get(Context.root())).isEqualTo(Attributes.empty());
    assertThat(ContextAttributes.get(null)).isEqualTo(Attributes.empty());
  }

  @Test
  void addNullOrEmptyAttributes_returnsSameContext() {
    Context root = Context.root();
    assertThat(ContextAttributes.add(root, null)).isSameAs(root);
    assertThat(ContextAttributes.add(root, Attributes.empty())).isSameAs(root);
    assertThat(ContextAttributes.add(null, Attributes.of(stringKey("k"), "v")))
        .isEqualTo(Context.root());
  }

  @Test
  void addAndGet_singleLayer() {
    Attributes attrs = Attributes.of(stringKey("user.id"), "u123", longKey("retry"), 1L);
    Context ctx = ContextAttributes.add(Context.root(), attrs);

    assertThat(ContextAttributes.get(ctx)).isEqualTo(attrs);
    assertThat(ContextAttributes.get(Context.root())).isEqualTo(Attributes.empty());
  }

  @Test
  void addAndGet_nestedContextsMergeAndOverride() {
    Context outer =
        ContextAttributes.add(
            Context.root(), Attributes.of(stringKey("k1"), "v1", stringKey("k2"), "v2"));
    Context inner =
        ContextAttributes.add(
            outer, Attributes.of(stringKey("k2"), "v2-override", stringKey("k3"), "v3"));

    assertThat(ContextAttributes.get(outer))
        .isEqualTo(Attributes.of(stringKey("k1"), "v1", stringKey("k2"), "v2"));
    assertThat(ContextAttributes.get(inner))
        .isEqualTo(
            Attributes.of(
                stringKey("k1"), "v1", stringKey("k2"), "v2-override", stringKey("k3"), "v3"));
  }

  @Test
  void scopeAttachment_restoresOuterContextAttributesOnClose() {
    Context outer = ContextAttributes.add(Context.root(), Attributes.of(stringKey("env"), "prod"));
    try (Scope ignoredOuter = outer.makeCurrent()) {
      assertThat(ContextAttributes.get(Context.current()))
          .isEqualTo(Attributes.of(stringKey("env"), "prod"));

      Context inner =
          ContextAttributes.add(
              Context.current(),
              Attributes.of(stringKey("env"), "staging", stringKey("req"), "r1"));
      try (Scope ignoredInner = inner.makeCurrent()) {
        assertThat(ContextAttributes.get(Context.current()))
            .isEqualTo(Attributes.of(stringKey("env"), "staging", stringKey("req"), "r1"));
      }

      assertThat(ContextAttributes.get(Context.current()))
          .isEqualTo(Attributes.of(stringKey("env"), "prod"));
    }
    assertThat(ContextAttributes.get(Context.current())).isEqualTo(Attributes.empty());
  }
}
