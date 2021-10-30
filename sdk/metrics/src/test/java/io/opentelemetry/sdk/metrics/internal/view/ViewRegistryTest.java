/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.view.Aggregation;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.View;
import org.junit.jupiter.api.Test;

class ViewRegistryTest {

  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("name", "version", "schema_url");

  @Test
  void selection_onType() {
    View view = View.builder().build();

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
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .hasSize(1)
        .element(0)
        .isEqualTo(view);
    // this one hasn't been configured, so it gets the default still.
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .hasSize(1)
        .element(0)
        .isSameAs(ViewRegistry.DEFAULT_VIEW);
  }

  @Test
  void selection_onName() {
    View view = View.builder().build();

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
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "overridden", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .hasSize(1)
        .element(0)
        .isSameAs(view);
    // this one hasn't been configured, so it gets the default still.
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "default", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .hasSize(1)
        .element(0)
        .isSameAs(ViewRegistry.DEFAULT_VIEW);
  }

  @Test
  void selection_FirstAddedViewWins() {
    View view1 = View.builder().setAggregation(Aggregation.lastValue()).build();
    View view2 = View.builder().setAggregation(Aggregation.histogram()).build();

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
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "overridden", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .hasSize(2)
        .element(0)
        .isEqualTo(view2);
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "default", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .hasSize(1)
        .element(0)
        .isEqualTo(view1);
  }

  @Test
  void selection_regex() {
    View view = View.builder().setAggregation(Aggregation.lastValue()).build();

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
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "overridden", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .hasSize(1)
        .element(0)
        .isEqualTo(view);
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "overrides", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .hasSize(1)
        .element(0)
        .isEqualTo(view);
    // this one hasn't been configured, so it gets the default still..
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "default", "", "", InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .hasSize(1)
        .element(0)
        .isSameAs(ViewRegistry.DEFAULT_VIEW);
  }

  @Test
  void defaults() {
    ViewRegistry viewRegistry = ViewRegistry.builder().build();
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .hasSize(1)
        .element(0)
        .isSameAs(ViewRegistry.DEFAULT_VIEW);
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .hasSize(1)
        .element(0)
        .isSameAs(ViewRegistry.DEFAULT_VIEW);
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.HISTOGRAM, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .hasSize(1)
        .element(0)
        .isSameAs(ViewRegistry.DEFAULT_VIEW);
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.OBSERVABLE_SUM, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .hasSize(1)
        .element(0)
        .isSameAs(ViewRegistry.DEFAULT_VIEW);
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.OBSERVABLE_GAUGE, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .hasSize(1)
        .element(0)
        .isSameAs(ViewRegistry.DEFAULT_VIEW);
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.OBSERVABLE_UP_DOWN_SUM, InstrumentValueType.LONG),
                INSTRUMENTATION_LIBRARY_INFO))
        .hasSize(1)
        .element(0)
        .isSameAs(ViewRegistry.DEFAULT_VIEW);
  }
}
