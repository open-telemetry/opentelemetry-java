/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.entry;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import org.junit.jupiter.api.Test;

class AdviceAttributesProcessorTest {

  @Test
  void doesNotUseContext() {
    assertThat(new AdviceAttributesProcessor(emptyList()).usesContext()).isFalse();
  }

  @Test
  void noExtraAttributes() {
    AttributesProcessor processor =
        new AdviceAttributesProcessor(asList(stringKey("abc"), stringKey("def")));

    Attributes result =
        processor.process(
            Attributes.builder().put(stringKey("abc"), "abc").put(stringKey("def"), "def").build(),
            Context.root());

    assertThat(result).containsOnly(entry(stringKey("abc"), "abc"), entry(stringKey("def"), "def"));
  }

  @Test
  void removeUnwantedAttributes() {
    AttributesProcessor processor =
        new AdviceAttributesProcessor(asList(stringKey("abc"), stringKey("def"), stringKey("ghi")));

    Attributes result =
        processor.process(
            Attributes.builder()
                .put(stringKey("abc"), "abc")
                .put(stringKey("def"), "def")
                .put(longKey("ghi"), 42L)
                .put(stringKey("xyz"), "xyz")
                .build(),
            Context.root());

    // does not contain key "ghi" because the type is different (stringKey != longKey)
    assertThat(result).containsOnly(entry(stringKey("abc"), "abc"), entry(stringKey("def"), "def"));
  }

  @Test
  void stringRepresentation() {
    assertThat(new AdviceAttributesProcessor(singletonList(stringKey("abc"))).toString())
        .isEqualTo("AdviceAttributesProcessor{attributeKeys=[abc]}");
  }
}
