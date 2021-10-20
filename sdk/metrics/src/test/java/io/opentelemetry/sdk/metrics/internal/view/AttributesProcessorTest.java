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

/** Tests for the {@link AttributesProcessors} DSL-ish library. */
public class AttributesProcessorTest {
  @Test
  public void filterKeyName_removesKeys() {
    AttributesProcessor processor =
        AttributesProcessor.filterByKeyName(name -> "test".equals(name));

    assertThat(
            processor.process(
                Attributes.builder().put("remove", "me").put("test", "keep").build(),
                Context.groot()))
        .hasSize(1)
        .containsEntry("test", "keep");
  }

  @Test
  public void append_works() {
    AttributesProcessor processor =
        AttributesProcessor.append(Attributes.builder().put("append", "me").build());
    assertThat(processor.process(Attributes.empty(), Context.groot()))
        .hasSize(1)
        .containsEntry("append", "me");
  }

  @Test
  public void append_doesNotOverrideExistingKeys() {
    AttributesProcessor processor =
        AttributesProcessor.append(Attributes.builder().put("test", "drop").build());
    assertThat(processor.process(Attributes.builder().put("test", "keep").build(), Context.groot()))
        .hasSize(1)
        .containsEntry("test", "keep");
  }

  @Test
  public void appendBaggage_works() {
    AttributesProcessor processor = AttributesProcessor.appendBaggageByKeyName(ignored -> true);
    Baggage baggage = Baggage.builder().put("baggage", "value").build();
    Context context = Context.groot().with(baggage);

    assertThat(processor.process(Attributes.builder().put("test", "keep").build(), context))
        .hasSize(2)
        .containsEntry("test", "keep")
        .containsEntry("baggage", "value");
  }

  @Test
  public void appendBaggage_doesNotOverrideExistingKeys() {
    AttributesProcessor processor = AttributesProcessor.appendBaggageByKeyName(ignored -> true);
    Baggage baggage = Baggage.builder().put("test", "drop").build();
    Context context = Context.groot().with(baggage);

    assertThat(processor.process(Attributes.builder().put("test", "keep").build(), context))
        .hasSize(1)
        .containsEntry("test", "keep");
  }

  @Test
  public void appendBaggageByKeyName_works() {
    AttributesProcessor processor =
        AttributesProcessor.appendBaggageByKeyName(name -> "keep".equals(name));
    Baggage baggage = Baggage.builder().put("baggage", "value").put("keep", "baggage").build();
    Context context = Context.groot().with(baggage);

    assertThat(processor.process(Attributes.builder().put("test", "keep").build(), context))
        .hasSize(2)
        .containsEntry("test", "keep")
        .containsEntry("keep", "baggage");
  }

  @Test
  public void proccessors_joinByThen() {
    // Baggage should be added, then all keys filtered.
    AttributesProcessor processor =
        AttributesProcessor.appendBaggageByKeyName(ignored -> true)
            .then(AttributesProcessor.filterByKeyName(name -> "baggage".equals(name)));
    Baggage baggage = Baggage.builder().put("baggage", "value").put("keep", "baggage").build();
    Context context = Context.groot().with(baggage);

    assertThat(processor.process(Attributes.builder().put("test", "keep").build(), context))
        .containsEntry("baggage", "value")
        .hasSize(1);
  }
}
