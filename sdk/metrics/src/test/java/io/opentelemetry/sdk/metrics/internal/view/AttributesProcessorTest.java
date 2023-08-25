/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import static io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor.append;
import static io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor.setIncludes;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Tests for the {@link AttributesProcessor} DSL-ish library. */
class AttributesProcessorTest {
  @Test
  void filterKeyName_Predicate() {
    AttributesProcessor processor = AttributesProcessor.filterByKeyName("test"::equals);

    assertThat(
            processor.process(
                Attributes.builder().put("remove", "me").put("test", "keep").build(),
                Context.root()))
        .hasSize(1)
        .containsEntry("test", "keep");
  }

  @Test
  void filterKeyName_SetIncludes() {
    AttributesProcessor processor =
        AttributesProcessor.filterByKeyName(setIncludes(Collections.singleton("test")));

    assertThat(
            processor.process(
                Attributes.builder().put("remove", "me").put("test", "keep").build(),
                Context.root()))
        .hasSize(1)
        .containsEntry("test", "keep");
  }

  @Test
  void filterKeyName_toString() {
    AttributesProcessor processor =
        AttributesProcessor.filterByKeyName(setIncludes(Collections.singleton("test")));

    assertThat(processor.toString())
        .isEqualTo("AttributeKeyFilteringProcessor{nameFilter=SetIncludesPredicate{set=[test]}}");
  }

  @Test
  void append_works() {
    AttributesProcessor processor =
        AttributesProcessor.append(Attributes.builder().put("append", "me").build());
    assertThat(processor.process(Attributes.empty(), Context.root()))
        .hasSize(1)
        .containsEntry("append", "me");
  }

  @Test
  void append_doesNotOverrideExistingKeys() {
    AttributesProcessor processor =
        AttributesProcessor.append(Attributes.builder().put("test", "drop").build());
    assertThat(processor.process(Attributes.builder().put("test", "keep").build(), Context.root()))
        .hasSize(1)
        .containsEntry("test", "keep");
  }

  @Test
  void append_toString() {
    AttributesProcessor processor =
        AttributesProcessor.append(Attributes.builder().put("key", "value").build());

    assertThat(processor.toString())
        .isEqualTo("AppendingAttributesProcessor{additionalAttributes={key=\"value\"}}");
  }

  @Test
  void appendBaggage_works() {
    AttributesProcessor processor = AttributesProcessor.appendBaggageByKeyName(ignored -> true);
    Baggage baggage = Baggage.builder().put("baggage", "value").build();
    Context context = Context.root().with(baggage);

    assertThat(processor.process(Attributes.builder().put("test", "keep").build(), context))
        .hasSize(2)
        .containsEntry("test", "keep")
        .containsEntry("baggage", "value");
  }

  @Test
  void appendBaggage_doesNotOverrideExistingKeys() {
    AttributesProcessor processor = AttributesProcessor.appendBaggageByKeyName(ignored -> true);
    Baggage baggage = Baggage.builder().put("test", "drop").build();
    Context context = Context.root().with(baggage);

    assertThat(processor.process(Attributes.builder().put("test", "keep").build(), context))
        .hasSize(1)
        .containsEntry("test", "keep");
  }

  @Test
  void appendBaggageByKeyName_works() {
    AttributesProcessor processor =
        AttributesProcessor.appendBaggageByKeyName(name -> "keep".equals(name));
    Baggage baggage = Baggage.builder().put("baggage", "value").put("keep", "baggage").build();
    Context context = Context.root().with(baggage);

    assertThat(processor.process(Attributes.builder().put("test", "keep").build(), context))
        .hasSize(2)
        .containsEntry("test", "keep")
        .containsEntry("keep", "baggage");
  }

  @Test
  void appendBaggage_toString() {
    AttributesProcessor processor =
        AttributesProcessor.appendBaggageByKeyName(setIncludes(Collections.singleton("keep")));

    assertThat(processor.toString())
        .isEqualTo(
            "BaggageAppendingAttributesProcessor{"
                + "nameFilter=SetIncludesPredicate{set=[keep]}"
                + "}");
  }

  @Test
  void proccessors_joinByThen() {
    // Baggage should be added, then all keys filtered.
    AttributesProcessor processor =
        AttributesProcessor.appendBaggageByKeyName(ignored -> true)
            .then(AttributesProcessor.filterByKeyName("baggage"::equals));
    Baggage baggage = Baggage.builder().put("baggage", "value").put("keep", "baggage").build();
    Context context = Context.root().with(baggage);

    assertThat(processor.process(Attributes.builder().put("test", "keep").build(), context))
        .containsEntry("baggage", "value")
        .hasSize(1);
  }

  @Test
  void joinedAttributes_toString() {
    AttributesProcessor processor =
        AttributesProcessor.appendBaggageByKeyName(setIncludes(Collections.singleton("keep")))
            .then(append(Attributes.builder().put("key", "value").build()));

    assertThat(processor.toString())
        .isEqualTo(
            "JoinedAttributesProcessor{processors=["
                + "BaggageAppendingAttributesProcessor{nameFilter=SetIncludesPredicate{set=[keep]}}, "
                + "AppendingAttributesProcessor{additionalAttributes={key=\"value\"}}"
                + "]}");
  }
}
