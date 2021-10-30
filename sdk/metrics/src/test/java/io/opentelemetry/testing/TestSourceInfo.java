/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.testing;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.internal.debug.SourceInfo;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.DebugUtils;
import io.opentelemetry.sdk.metrics.view.View;
import org.junit.jupiter.api.Test;

// Note: This class MUST be outside the io.opentelemetry.metrics package to work correctly.
class TestSourceInfo {
  // Note: The line numbers for these statics are used as part of the test.
  private static final SourceInfo info = SourceInfo.fromCurrentStack();

  @Test
  void sourceInfoFindsStackTrace() {
    assertThat(info.shortDebugString()).isEqualTo("TestSourceInfo.java:22");
    assertThat(info.multiLineDebugString())
        .startsWith(
            "\tat io.opentelemetry.testing.TestSourceInfo.<clinit>(TestSourceInfo.java:22)\n");
  }

  @Test
  void sourceInfoUsesCustomValues() {
    SourceInfo info = SourceInfo.fromConfigFile("mypath.yml", 20);
    assertThat(info.shortDebugString()).isEqualTo("mypath.yml:20");
    assertThat(info.multiLineDebugString()).isEqualTo("\tat mypath.yml:20");
  }

  @Test
  void testDuplicateExceptionMessage_pureInstruments() {
    MetricDescriptor simple =
        MetricDescriptor.create(
            View.builder().build(),
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit",
                InstrumentType.HISTOGRAM,
                InstrumentValueType.DOUBLE));
    MetricDescriptor simpleWithNewDescription =
        MetricDescriptor.create(
            View.builder().build(),
            InstrumentDescriptor.create(
                "name",
                "description2",
                "unit2",
                InstrumentType.HISTOGRAM,
                InstrumentValueType.DOUBLE));
    assertThat(DebugUtils.duplicateMetricErrorMessage(simple, simpleWithNewDescription))
        .contains("Found duplicate metric definition: name")
        .contains("- Unit [unit2] does not match [unit]")
        .contains("- Description [description2] does not match [description]")
        .contains(simple.getSourceInstrument().getSourceInfo().multiLineDebugString())
        .contains("Original instrument registered with same name but different description or unit")
        .contains(
            simpleWithNewDescription.getSourceInstrument().getSourceInfo().multiLineDebugString());
  }

  @Test
  void testDuplicateExceptionMessage_viewBasedConflict() {
    View problemView = View.builder().setName("name2").build();
    MetricDescriptor simple =
        MetricDescriptor.create(
            problemView,
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit",
                InstrumentType.HISTOGRAM,
                InstrumentValueType.DOUBLE));
    MetricDescriptor simpleWithNewDescription =
        MetricDescriptor.create(
            View.builder().build(),
            InstrumentDescriptor.create(
                "name2",
                "description2",
                "unit",
                InstrumentType.HISTOGRAM,
                InstrumentValueType.DOUBLE));
    assertThat(DebugUtils.duplicateMetricErrorMessage(simple, simpleWithNewDescription))
        .contains("Found duplicate metric definition: name2")
        .contains(simple.getSourceInstrument().getSourceInfo().multiLineDebugString())
        .contains("- Description [description2] does not match [description]")
        .contains("Conflicting view registered")
        .contains(problemView.getSourceInfo().multiLineDebugString())
        .contains("FROM instrument name")
        .contains(
            simpleWithNewDescription.getSourceInstrument().getSourceInfo().multiLineDebugString());
  }

  @Test
  void testDuplicateExceptionMessage_viewBasedConflict2() {
    View problemView = View.builder().setName("name").build();
    MetricDescriptor simple =
        MetricDescriptor.create(
            View.builder().build(),
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit2",
                InstrumentType.HISTOGRAM,
                InstrumentValueType.DOUBLE));
    MetricDescriptor simpleWithNewDescription =
        MetricDescriptor.create(
            problemView,
            InstrumentDescriptor.create(
                "name2",
                "description",
                "unit",
                InstrumentType.HISTOGRAM,
                InstrumentValueType.DOUBLE));
    assertThat(DebugUtils.duplicateMetricErrorMessage(simple, simpleWithNewDescription))
        .contains("Found duplicate metric definition: name")
        .contains("VIEW defined")
        .contains(problemView.getSourceInfo().multiLineDebugString())
        .contains("FROM instrument name2")
        .contains(simple.getSourceInstrument().getSourceInfo().multiLineDebugString())
        .contains("- Unit [unit] does not match [unit2]")
        .contains("Original instrument registered with same name but different description or unit")
        .contains(
            simpleWithNewDescription.getSourceInstrument().getSourceInfo().multiLineDebugString());
  }
}
