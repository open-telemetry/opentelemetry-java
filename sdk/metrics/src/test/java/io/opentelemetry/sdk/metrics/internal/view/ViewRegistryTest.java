/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import static io.opentelemetry.sdk.metrics.internal.view.ViewRegistry.toGlobPatternPredicate;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.internal.debug.SourceInfo;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ViewRegistryTest {

  @RegisterExtension LogCapturer logs = LogCapturer.create().captureForType(ViewRegistry.class);

  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.create("name", "version", "schema_url");

  @Test
  void findViews_SelectionOnType() {
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

    assertThat(logs.getEvents()).hasSize(0);
  }

  @Test
  void findViews_SelectionOnName() {
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

    assertThat(logs.getEvents()).hasSize(0);
  }

  @Test
  void findViews_MultipleMatchingViews() {
    View view1 = View.builder().setAggregation(Aggregation.sum()).build();
    View view2 = View.builder().setAggregation(Aggregation.explicitBucketHistogram()).build();

    ViewRegistry viewRegistry =
        ViewRegistry.builder()
            .addView(
                InstrumentSelector.builder().setName("overridden").build(),
                view2,
                AttributesProcessor.noop(),
                SourceInfo.fromCurrentStack())
            .addView(
                InstrumentSelector.builder().setName("*").build(),
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

    assertThat(logs.getEvents()).hasSize(0);
  }

  @Test
  void findViews_SelectionTypeAndName() {
    View view = View.builder().setAggregation(Aggregation.explicitBucketHistogram()).build();

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

    assertThat(logs.getEvents()).hasSize(0);
  }

  @Test
  @SuppressLogger(ViewRegistry.class)
  void findViews_IncompatibleViewIgnored() {
    View view = View.builder().setAggregation(Aggregation.explicitBucketHistogram()).build();

    ViewRegistry viewRegistry =
        ViewRegistry.builder()
            .addView(
                InstrumentSelector.builder().setType(InstrumentType.OBSERVABLE_COUNTER).build(),
                view,
                AttributesProcessor.noop(),
                SourceInfo.fromCurrentStack())
            .build();

    assertThat(
            viewRegistry.findViews(
                InstrumentDescriptor.create(
                    "test", "", "", InstrumentType.OBSERVABLE_COUNTER, InstrumentValueType.LONG),
                INSTRUMENTATION_SCOPE_INFO))
        .hasSize(1)
        .element(0)
        .isEqualTo(ViewRegistry.DEFAULT_REGISTERED_VIEW);

    logs.assertContains(
        "View aggregation explicit_bucket_histogram is incompatible with instrument test of type OBSERVABLE_COUNTER");
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

    assertThat(logs.getEvents()).hasSize(0);
  }

  @Test
  void matchesName() {
    assertThat(toGlobPatternPredicate("foo").test("foo")).isTrue();
    assertThat(toGlobPatternPredicate("foo").test("Foo")).isTrue();
    assertThat(toGlobPatternPredicate("foo").test("bar")).isFalse();
    assertThat(toGlobPatternPredicate("fo?").test("foo")).isTrue();
    assertThat(toGlobPatternPredicate("fo??").test("fooo")).isTrue();
    assertThat(toGlobPatternPredicate("fo?").test("fob")).isTrue();
    assertThat(toGlobPatternPredicate("fo?").test("fooo")).isFalse();
    assertThat(toGlobPatternPredicate("*").test("foo")).isTrue();
    assertThat(toGlobPatternPredicate("*").test("bar")).isTrue();
    assertThat(toGlobPatternPredicate("*").test("baz")).isTrue();
    assertThat(toGlobPatternPredicate("*").test("foo.bar.baz")).isTrue();
    assertThat(toGlobPatternPredicate("fo*").test("fo")).isTrue();
    assertThat(toGlobPatternPredicate("fo*").test("foo")).isTrue();
    assertThat(toGlobPatternPredicate("fo*").test("fooo")).isTrue();
    assertThat(toGlobPatternPredicate("fo*").test("foo.bar.baz")).isTrue();
    assertThat(toGlobPatternPredicate("f()[]$^.{}|").test("f()[]$^.{}|")).isTrue();
    assertThat(toGlobPatternPredicate("f()[]$^.{}|?").test("f()[]$^.{}|o")).isTrue();
    assertThat(toGlobPatternPredicate("f()[]$^.{}|*").test("f()[]$^.{}|ooo")).isTrue();
  }
}
