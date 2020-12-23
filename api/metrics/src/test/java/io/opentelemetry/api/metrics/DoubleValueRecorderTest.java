/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.DoubleValueRecorder.BoundDoubleValueRecorder;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class DoubleValueRecorderTest {

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final Meter meter = DefaultMeter.getInstance();

  @Test
  void preventNull_Name() {
    assertThatThrownBy(() -> meter.doubleValueRecorderBuilder(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("name");
  }

  @Test
  void preventEmpty_Name() {
    assertThatThrownBy(() -> meter.doubleValueRecorderBuilder("").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNonPrintableName() {
    assertThatThrownBy(() -> meter.doubleValueRecorderBuilder("\2").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventTooLongName() {
    char[] chars = new char[256];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    assertThatThrownBy(() -> meter.doubleValueRecorderBuilder(longName).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNull_Description() {
    assertThatThrownBy(
            () -> meter.doubleValueRecorderBuilder("metric").setDescription(null).build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("description");
  }

  @Test
  void preventNull_Unit() {
    assertThatThrownBy(() -> meter.doubleValueRecorderBuilder("metric").setUnit(null).build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
  }

  @Test
  void record_PreventNullLabels() {
    assertThatThrownBy(() -> meter.doubleValueRecorderBuilder("metric").build().record(1.0, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
  }

  @Test
  void record_DoesNotThrow() {
    DoubleValueRecorder doubleValueRecorder =
        meter.doubleValueRecorderBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    doubleValueRecorder.record(5.0, Labels.empty());
    doubleValueRecorder.record(-5.0, Labels.empty());
    doubleValueRecorder.record(5.0);
    doubleValueRecorder.record(-5.0);
  }

  @Test
  void bound_PreventNullLabels() {
    assertThatThrownBy(() -> meter.doubleValueRecorderBuilder("metric").build().bind(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
  }

  @Test
  void bound_DoesNotThrow() {
    DoubleValueRecorder doubleValueRecorder =
        meter.doubleValueRecorderBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    BoundDoubleValueRecorder bound = doubleValueRecorder.bind(Labels.empty());
    bound.record(5.0);
    bound.record(-5.0);
    bound.unbind();
  }
}
