/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.common;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
  void addNullValidation() {
    Context root = Context.root();
    assertThatThrownBy(() -> ContextAttributes.add(null, Attributes.of(stringKey("k"), "v")))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("context");
    assertThatThrownBy(() -> ContextAttributes.add(root, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("attributes");
  }

  @Test
  void addEmptyAttributes_returnsSameContext() {
    Context root = Context.root();
    assertThat(ContextAttributes.add(root, Attributes.empty())).isSameAs(root);
  }

  @Test
  void setNullValidation() {
    Context root = Context.root();
    assertThatThrownBy(() -> ContextAttributes.set(null, Attributes.of(stringKey("k"), "v")))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("context");
    assertThatThrownBy(() -> ContextAttributes.set(root, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("attributes");
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
  void addWithSameNameDifferentType_overwritesExisting() {
    Context ctx1 =
        ContextAttributes.add(Context.root(), Attributes.of(stringKey("foo"), "stringVal"));
    Context ctx2 = ContextAttributes.add(ctx1, Attributes.of(longKey("foo"), 42L));
    assertThat(ContextAttributes.get(ctx2)).isEqualTo(Attributes.of(longKey("foo"), 42L));
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
