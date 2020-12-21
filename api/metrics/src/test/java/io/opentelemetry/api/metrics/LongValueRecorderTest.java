/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import static io.opentelemetry.api.metrics.DefaultMeter.ERROR_MESSAGE_INVALID_NAME;
import static java.util.Arrays.fill;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.LongValueRecorder.BoundLongValueRecorder;
import org.junit.jupiter.api.Test;

/** Tests for {@link LongValueRecorder}. */
public final class LongValueRecorderTest {

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final Meter meter = DefaultMeter.getInstance();

  @Test
  void preventNull_Name() {
    assertThatThrownBy(() -> meter.longValueRecorderBuilder(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("name");
  }

  @Test
  void preventEmpty_Name() {
    assertThatThrownBy(() -> meter.longValueRecorderBuilder("").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNonPrintableMeasureName() {
    assertThatThrownBy(() -> meter.longValueRecorderBuilder("\2").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventTooLongName() {
    char[] chars = new char[256];
    fill(chars, 'a');
    String longName = String.valueOf(chars);
    assertThatThrownBy(() -> meter.longValueRecorderBuilder(longName).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNull_Description() {
    assertThatThrownBy(() -> meter.longValueRecorderBuilder("metric").setDescription(null).build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("description");
  }

  @Test
  void preventNull_Unit() {
    assertThatThrownBy(() -> meter.longValueRecorderBuilder("metric").setUnit(null).build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
  }

  @Test
  void record_PreventNullLabels() {
    assertThatThrownBy(() -> meter.longValueRecorderBuilder("metric").build().record(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
  }

  @Test
  void record_DoesNotThrow() {
    LongValueRecorder longValueRecorder =
        meter.longValueRecorderBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    longValueRecorder.record(5, Labels.empty());
    longValueRecorder.record(-5, Labels.empty());
    longValueRecorder.record(5);
    longValueRecorder.record(-5);
  }

  @Test
  void bound_PreventNullLabels() {
    assertThatThrownBy(() -> meter.longValueRecorderBuilder("metric").build().bind(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
  }

  @Test
  void bound_DoesNotThrow() {
    LongValueRecorder longValueRecorder =
        meter.longValueRecorderBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    BoundLongValueRecorder bound = longValueRecorder.bind(Labels.empty());
    bound.record(5);
    bound.record(-5);
    bound.unbind();
  }
}
