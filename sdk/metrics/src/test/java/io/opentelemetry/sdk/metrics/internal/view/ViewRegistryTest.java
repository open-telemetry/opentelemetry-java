/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.internal.debug.SourceInfo;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import org.junit.jupiter.api.Test;

class ViewRegistryTest {

  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.create("name", "version", "schema_url");

  @Test
  void selection_onType() {
    View view = View.builder().build();

    ViewRegistry viewRegistry =
        ViewRegistry.builder()
            .addView(
                InstrumentSelector.builder().setType(InstrumentType.COUNTER).build(),
                view,
                AttributesProcessor.noop(),
                SourceInfo.fromCurrentStack())
            .build();
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_SCOPE_INFO))
        .hasSize(1)
        .element(0)
        .extracting(RegisteredView::getView)
        .isEqualTo(view);
    // this one hasn't been configured, so it gets the default still.
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_SCOPE_INFO))
        .hasSize(1)
        .element(0)
        .isSameAs(ViewRegistry.DEFAULT_REGISTERED_VIEW);
  }

  @Test
  void selection_onName() {
    View view = View.builder().build();

    ViewRegistry viewRegistry =
        ViewRegistry.builder()
            .addView(
                InstrumentSelector.builder().setName("overridden").build(),
                view,
                AttributesProcessor.noop(),
                SourceInfo.fromCurrentStack())
            .build();
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "overridden", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_SCOPE_INFO))
        .hasSize(1)
        .element(0)
        .extracting(RegisteredView::getView)
        .isSameAs(view);
    // this one hasn't been configured, so it gets the default still.
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "default", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_SCOPE_INFO))
        .hasSize(1)
        .element(0)
        .isSameAs(ViewRegistry.DEFAULT_REGISTERED_VIEW);
  }

  @Test
  void selection_FirstAddedViewWins() {
    View view1 = View.builder().setAggregation(Aggregation.lastValue()).build();
    View view2 = View.builder().setAggregation(Aggregation.explicitBucketHistogram()).build();

    ViewRegistry viewRegistry =
        ViewRegistry.builder()
            .addView(
                InstrumentSelector.builder().setName(name -> name.equals("overridden")).build(),
                view2,
                AttributesProcessor.noop(),
                SourceInfo.fromCurrentStack())
            .addView(
                InstrumentSelector.builder().setName(name -> true).build(),
                view1,
                AttributesProcessor.noop(),
                SourceInfo.fromCurrentStack())
            .build();

    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "overridden", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_SCOPE_INFO))
        .hasSize(2)
        .element(0)
        .extracting(RegisteredView::getView)
        .isEqualTo(view2);
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "default", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_SCOPE_INFO))
        .hasSize(1)
        .element(0)
        .extracting(RegisteredView::getView)
        .isEqualTo(view1);
  }

  @Test
  void selection_typeAndName() {
    View view = View.builder().setAggregation(Aggregation.lastValue()).build();

    ViewRegistry viewRegistry =
        ViewRegistry.builder()
            .addView(
                InstrumentSelector.builder()
                    .setType(InstrumentType.COUNTER)
                    .setName("overrides")
                    .build(),
                view,
                AttributesProcessor.noop(),
                SourceInfo.fromCurrentStack())
            .build();

    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "overrides", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_SCOPE_INFO))
        .hasSize(1)
        .element(0)
        .extracting(RegisteredView::getView)
        .isEqualTo(view);
    // this one hasn't been configured, so it gets the default still..
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "overrides", "", "", InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_SCOPE_INFO))
        .hasSize(1)
        .element(0)
        .isEqualTo(ViewRegistry.DEFAULT_REGISTERED_VIEW);
    // this one hasn't been configured, so it gets the default still..
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "default", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_SCOPE_INFO))
        .hasSize(1)
        .element(0)
        .isEqualTo(ViewRegistry.DEFAULT_REGISTERED_VIEW);
  }

  @Test
  void defaults() {
    ViewRegistry viewRegistry = ViewRegistry.builder().build();
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_SCOPE_INFO))
        .hasSize(1)
        .element(0)
        .isSameAs(ViewRegistry.DEFAULT_REGISTERED_VIEW);
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_SCOPE_INFO))
        .hasSize(1)
        .element(0)
        .isSameAs(ViewRegistry.DEFAULT_REGISTERED_VIEW);
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.HISTOGRAM, InstrumentValueType.LONG),
                INSTRUMENTATION_SCOPE_INFO))
        .hasSize(1)
        .element(0)
        .isSameAs(ViewRegistry.DEFAULT_REGISTERED_VIEW);
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.OBSERVABLE_COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_SCOPE_INFO))
        .hasSize(1)
        .element(0)
        .isSameAs(ViewRegistry.DEFAULT_REGISTERED_VIEW);
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.OBSERVABLE_GAUGE, InstrumentValueType.LONG),
                INSTRUMENTATION_SCOPE_INFO))
        .hasSize(1)
        .element(0)
        .isSameAs(ViewRegistry.DEFAULT_REGISTERED_VIEW);
    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "",
                    "",
                    "",
                    InstrumentType.OBSERVABLE_UP_DOWN_COUNTER,
                    InstrumentValueType.LONG),
                INSTRUMENTATION_SCOPE_INFO))
        .hasSize(1)
        .element(0)
        .isSameAs(ViewRegistry.DEFAULT_REGISTERED_VIEW);
  }
}
