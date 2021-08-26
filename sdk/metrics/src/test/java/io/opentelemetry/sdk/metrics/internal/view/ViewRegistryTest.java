/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.View;
import org.junit.jupiter.api.Test;

class ViewRegistryTest {

  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("name", "version", "schema_url");

  @Test
  void selection_onType() {
    AggregatorFactory factory = AggregatorFactory.lastValue();
    View view = View.builder().setAggregatorFactory(factory).build();

    ViewRegistry viewRegistry =
        ViewRegistry.builder()
            .addView(
                InstrumentSelector.builder()
                    .setInstrumentType(InstrumentType.COUNTER)
                    .setInstrumentNameRegex(".*")
                    .build(),
                view)
            .build();
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .isEqualTo(view);
    // this one hasn't been configured, so it gets the default still.
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .isSameAs(ViewRegistry.CUMULATIVE_SUM);
  }

  @Test
  void selection_onName() {
    AggregatorFactory factory = AggregatorFactory.lastValue();
    View view = View.builder().setAggregatorFactory(factory).build();

    ViewRegistry viewRegistry =
        ViewRegistry.builder()
            .addView(
                InstrumentSelector.builder()
                    .setInstrumentType(InstrumentType.COUNTER)
                    .setInstrumentNameRegex("overridden")
                    .build(),
                view)
            .build();
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "overridden", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .isSameAs(view);
    // this one hasn't been configured, so it gets the default still.
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "default", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .isSameAs(ViewRegistry.CUMULATIVE_SUM);
  }

  @Test
  void selection_FirstAddedViewWins() {
    AggregatorFactory factory1 = AggregatorFactory.lastValue();
    View view1 = View.builder().setAggregatorFactory(factory1).build();
    AggregatorFactory factory2 = AggregatorFactory.minMaxSumCount();
    View view2 = View.builder().setAggregatorFactory(factory2).build();

    ViewRegistry viewRegistry =
        ViewRegistry.builder()
            .addView(
                InstrumentSelector.builder()
                    .setInstrumentType(InstrumentType.COUNTER)
                    .setInstrumentNameRegex("overridden")
                    .build(),
                view2)
            .addView(
                InstrumentSelector.builder()
                    .setInstrumentType(InstrumentType.COUNTER)
                    .setInstrumentNameRegex(".*")
                    .build(),
                view1)
            .build();

    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "overridden", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .isEqualTo(view2);
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "default", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .isEqualTo(view1);
  }

  @Test
  void selection_regex() {
    AggregatorFactory factory = AggregatorFactory.lastValue();
    View view = View.builder().setAggregatorFactory(factory).build();

    ViewRegistry viewRegistry =
        ViewRegistry.builder()
            .addView(
                InstrumentSelector.builder()
                    .setInstrumentNameRegex("overrid(es|den)")
                    .setInstrumentType(InstrumentType.COUNTER)
                    .build(),
                view)
            .build();

    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "overridden", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .isEqualTo(view);
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "overrides", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .isEqualTo(view);
    // this one hasn't been configured, so it gets the default still..
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "default", "", "", InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .isSameAs(ViewRegistry.CUMULATIVE_SUM);
  }

  @Test
  void defaults() {
    ViewRegistry viewRegistry = ViewRegistry.builder().build();
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .isSameAs(ViewRegistry.CUMULATIVE_SUM);
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .isSameAs(ViewRegistry.CUMULATIVE_SUM);
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.HISTOGRAM, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .isSameAs(ViewRegistry.DEFAULT_HISTOGRAM);
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.OBSERVABLE_SUM, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .isSameAs(ViewRegistry.CUMULATIVE_SUM);
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.OBSERVABLE_GAUGE, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .isSameAs(ViewRegistry.LAST_VALUE);
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.OBSERVABLE_UP_DOWN_SUM, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .isSameAs(ViewRegistry.CUMULATIVE_SUM);
  }
}
