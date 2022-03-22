/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.testing;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.internal.debug.SourceInfo;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.DebugUtils;
import org.junit.jupiter.api.Test;

// Note: This class MUST be outside the io.opentelemetry.metrics package to work correctly.
class SourceInfoTest {
  // Note: The line numbers for these statics are used as part of the test.
  private static final SourceInfo info = SourceInfo.fromCurrentStack();

  @Test
  void sourceInfoFindsStackTrace() {
    assertThat(info.shortDebugString()).isEqualTo("SourceInfoTest.java:22");
    assertThat(info.multiLineDebugString())
        .startsWith(
            "\tat io.opentelemetry.testing.SourceInfoTest.<clinit>(SourceInfoTest.java:22)\n");
  }

  @Test
  void testDuplicateExceptionMessage_pureInstruments() {
    MetricDescriptor simple =
        MetricDescriptor.create(
            View.builder().build(),
            SourceInfo.fromCurrentStack(),
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit",
                InstrumentType.OBSERVABLE_COUNTER,
                InstrumentValueType.DOUBLE));
    MetricDescriptor simpleWithNewDescription =
        MetricDescriptor.create(
            View.builder().build(),
            SourceInfo.fromCurrentStack(),
            InstrumentDescriptor.create(
                "name2",
                "description2",
                "unit2",
                InstrumentType.COUNTER,
                InstrumentValueType.LONG));
    assertThat(DebugUtils.duplicateMetricErrorMessage(simple, simpleWithNewDescription))
        .contains("Found duplicate metric definition: name")
        .contains("- InstrumentDescription [description2] does not match [description]")
        .contains("- InstrumentName [name2] does not match [name]")
        .contains("- InstrumentUnit [unit2] does not match [unit]")
        .contains("- InstrumentType [COUNTER] does not match [OBSERVABLE_COUNTER]")
        .contains("- InstrumentValueType [LONG] does not match [DOUBLE]")
        .contains(simple.getSourceInstrument().getSourceInfo().multiLineDebugString())
        .contains("Original instrument registered with same name but is incompatible.")
        .contains(
            simpleWithNewDescription.getSourceInstrument().getSourceInfo().multiLineDebugString());
  }

  @Test
  void testDuplicateExceptionMessage_viewBasedConflict() {
    SourceInfo simpleSourceInfo = SourceInfo.fromCurrentStack();
    MetricDescriptor simple =
        MetricDescriptor.create(
            View.builder().setName("name2").build(),
            simpleSourceInfo,
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit",
                InstrumentType.HISTOGRAM,
                InstrumentValueType.DOUBLE));
    MetricDescriptor simpleWithNewDescription =
        MetricDescriptor.create(
            View.builder().build(),
            SourceInfo.fromCurrentStack(),
            InstrumentDescriptor.create(
                "name2",
                "description2",
                "unit",
                InstrumentType.HISTOGRAM,
                InstrumentValueType.DOUBLE));
    assertThat(DebugUtils.duplicateMetricErrorMessage(simple, simpleWithNewDescription))
        .contains("Found duplicate metric definition: name2")
        .contains(simple.getSourceInstrument().getSourceInfo().multiLineDebugString())
        .contains("- InstrumentDescription [description2] does not match [description]")
        .contains("Conflicting view registered")
        .contains(simpleSourceInfo.multiLineDebugString())
        .contains("FROM instrument name")
        .contains(
            simpleWithNewDescription.getSourceInstrument().getSourceInfo().multiLineDebugString());
  }

  @Test
  void testDuplicateExceptionMessage_viewBasedConflict2() {
    SourceInfo problemViewSourceInfo = SourceInfo.fromCurrentStack();
    View problemView = View.builder().setName("name").build();
    MetricDescriptor simple =
        MetricDescriptor.create(
            View.builder().build(),
            SourceInfo.fromCurrentStack(),
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit2",
                InstrumentType.HISTOGRAM,
                InstrumentValueType.DOUBLE));
    MetricDescriptor simpleWithNewDescription =
        MetricDescriptor.create(
            problemView,
            problemViewSourceInfo,
            InstrumentDescriptor.create(
                "name2",
                "description",
                "unit",
                InstrumentType.HISTOGRAM,
                InstrumentValueType.DOUBLE));
    assertThat(DebugUtils.duplicateMetricErrorMessage(simple, simpleWithNewDescription))
        .contains("Found duplicate metric definition: name")
        .contains("VIEW defined")
        .contains(problemViewSourceInfo.multiLineDebugString())
        .contains("FROM instrument name2")
        .contains(simple.getSourceInstrument().getSourceInfo().multiLineDebugString())
        .contains("- InstrumentUnit [unit] does not match [unit2]")
        .contains("Original instrument registered with same name but is incompatible.")
        .contains(
            simpleWithNewDescription.getSourceInstrument().getSourceInfo().multiLineDebugString());
  }
}
