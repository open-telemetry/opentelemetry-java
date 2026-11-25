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
  void setNullValidation() {
    Context root = Context.root();
    Attributes attrs = Attributes.of(stringKey("k"), "v");
    assertThat(ContextAttributes.set(null, attrs)).isEqualTo(Context.root());
    assertThat(ContextAttributes.set(root, null)).isSameAs(root);
  }

  @Test
  void setReplacesExistingAndClearsWithEmpty() {
    Context ctx1 =
        ContextAttributes.set(
            Context.root(), Attributes.of(stringKey("k1"), "v1", longKey("k2"), 2L));
    assertThat(ContextAttributes.get(ctx1))
        .isEqualTo(Attributes.of(stringKey("k1"), "v1", longKey("k2"), 2L));

    // set() completely replaces previous attributes
    Context ctx2 = ContextAttributes.set(ctx1, Attributes.of(stringKey("k3"), "v3"));
    assertThat(ContextAttributes.get(ctx2)).isEqualTo(Attributes.of(stringKey("k3"), "v3"));

    // set() with empty clears attributes
    Context ctxCleared = ContextAttributes.set(ctx2, Attributes.empty());
    assertThat(ContextAttributes.get(ctxCleared)).isEqualTo(Attributes.empty());

    // set() with empty on already empty returns same context
    assertThat(ContextAttributes.set(Context.root(), Attributes.empty())).isSameAs(Context.root());
  }

  @Test
  void setAndGet_singleLayer() {
    Attributes attrs = Attributes.of(stringKey("user.id"), "u123", longKey("retry"), 1L);
    Context ctx = ContextAttributes.set(Context.root(), attrs);

    assertThat(ContextAttributes.get(ctx)).isEqualTo(attrs);
    assertThat(ContextAttributes.get(Context.root())).isEqualTo(Attributes.empty());
  }

  @Test
  void scopeAttachment_restoresOuterContextAttributesOnClose() {
    Context outer = ContextAttributes.set(Context.root(), Attributes.of(stringKey("env"), "prod"));
    try (Scope ignoredOuter = outer.makeCurrent()) {
      assertThat(ContextAttributes.get(Context.current()))
          .isEqualTo(Attributes.of(stringKey("env"), "prod"));

      Context inner =
          ContextAttributes.set(
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
