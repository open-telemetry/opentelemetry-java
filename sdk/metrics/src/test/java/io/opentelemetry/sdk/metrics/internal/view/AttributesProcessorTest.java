/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import org.junit.jupiter.api.Test;

/** Tests for the {@link AbstractAttributesProcessor} DSL-ish library. */
public class AttributesProcessorTest {
  @Test
  public void filterKeyName_removesKeys() {
    AbstractAttributesProcessor processor =
        AbstractAttributesProcessor.filterByKeyName(name -> "test".equals(name));

    assertThat(
            processor.process(
                Attributes.builder().put("remove", "me").put("test", "keep").build(),
                Context.root()))
        .hasSize(1)
        .containsEntry("test", "keep");
  }

  @Test
  public void append_works() {
    AbstractAttributesProcessor processor =
        AbstractAttributesProcessor.append(Attributes.builder().put("append", "me").build());
    assertThat(processor.process(Attributes.empty(), Context.root()))
        .hasSize(1)
        .containsEntry("append", "me");
  }

  @Test
  public void append_doesNotOverrideExistingKeys() {
    AbstractAttributesProcessor processor =
        AbstractAttributesProcessor.append(Attributes.builder().put("test", "drop").build());
    assertThat(processor.process(Attributes.builder().put("test", "keep").build(), Context.root()))
        .hasSize(1)
        .containsEntry("test", "keep");
  }

  @Test
  public void appendBaggage_works() {
    AbstractAttributesProcessor processor =
        AbstractAttributesProcessor.appendBaggageByKeyName(ignored -> true);
    Baggage baggage = Baggage.builder().put("baggage", "value").build();
    Context context = Context.root().with(baggage);

    assertThat(processor.process(Attributes.builder().put("test", "keep").build(), context))
        .hasSize(2)
        .containsEntry("test", "keep")
        .containsEntry("baggage", "value");
  }

  @Test
  public void appendBaggage_doesNotOverrideExistingKeys() {
    AbstractAttributesProcessor processor =
        AbstractAttributesProcessor.appendBaggageByKeyName(ignored -> true);
    Baggage baggage = Baggage.builder().put("test", "drop").build();
    Context context = Context.root().with(baggage);

    assertThat(processor.process(Attributes.builder().put("test", "keep").build(), context))
        .hasSize(1)
        .containsEntry("test", "keep");
  }

  @Test
  public void appendBaggageByKeyName_works() {
    AbstractAttributesProcessor processor =
        AbstractAttributesProcessor.appendBaggageByKeyName(name -> "keep".equals(name));
    Baggage baggage = Baggage.builder().put("baggage", "value").put("keep", "baggage").build();
    Context context = Context.root().with(baggage);

    assertThat(processor.process(Attributes.builder().put("test", "keep").build(), context))
        .hasSize(2)
        .containsEntry("test", "keep")
        .containsEntry("keep", "baggage");
  }

  @Test
  public void proccessors_joinByThen() {
    // Baggage should be added, then all keys filtered.
    AbstractAttributesProcessor processor =
        AbstractAttributesProcessor.appendBaggageByKeyName(ignored -> true)
            .then(AbstractAttributesProcessor.filterByKeyName(name -> "baggage".equals(name)));
    Baggage baggage = Baggage.builder().put("baggage", "value").put("keep", "baggage").build();
    Context context = Context.root().with(baggage);

    assertThat(processor.process(Attributes.builder().put("test", "keep").build(), context))
        .containsEntry("baggage", "value")
        .hasSize(1);
  }
}
