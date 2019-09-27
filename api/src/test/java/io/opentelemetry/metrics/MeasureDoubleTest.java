/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.metrics;

import io.opentelemetry.distributedcontext.DefaultDistributedContextManager;
import io.opentelemetry.distributedcontext.DistributedContextManager;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link MeasureDouble}. */
@RunWith(JUnit4.class)
public final class MeasureDoubleTest {
  private static final Meter meter = DefaultMeter.getInstance();
  private static final DistributedContextManager distContextManager =
      DefaultDistributedContextManager.getInstance();

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void preventNonPrintableName() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
    meter.measureDoubleBuilder("\2").build();
  }

  @Test
  public void preventTooLongName() {
    char[] chars = new char[256];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
    meter.measureDoubleBuilder(longName).build();
  }

  @Test
  public void preventNull_Description() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("description");
    meter.measureDoubleBuilder("metric").setDescription(null).build();
  }

  @Test
  public void preventNull_Unit() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("unit");
    meter.measureDoubleBuilder("metric").setUnit(null).build();
  }

  @Test
  public void preventNull_LabelKeys() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelKeys");
    meter.measureDoubleBuilder("metric").setLabelKeys(null).build();
  }

  @Test
  public void preventNull_LabelKey() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelKey");
    meter
        .measureDoubleBuilder("metric")
        .setLabelKeys(Collections.<String>singletonList(null))
        .build();
  }

  @Test
  public void preventNull_ConstantLabels() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("constantLabels");
    meter.measureDoubleBuilder("metric").setConstantLabels(null).build();
  }

  @Test
  public void preventNegativeValue() {
    MeasureDouble myMeasure = meter.measureDoubleBuilder("MyMeasure").build();
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Unsupported negative values");
    myMeasure.getDefaultHandle().record(-5.0);
  }

  @Test
  public void doesNotThrow() {
    MeasureDouble myMeasure = meter.measureDoubleBuilder("MyMeasure").build();
    myMeasure.getDefaultHandle().record(5.0);
  }

  @Test
  public void preventNegativeValue_RecordWithContext() {
    MeasureDouble myMeasure = meter.measureDoubleBuilder("MyMeasure").build();
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Unsupported negative values");
    myMeasure.getDefaultHandle().record(-5.0, distContextManager.getCurrentContext());
  }

  @Test
  public void preventNullDistContext_RecordWithContext() {
    MeasureDouble myMeasure = meter.measureDoubleBuilder("MyMeasure").build();
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("distContext");
    myMeasure.getDefaultHandle().record(5.0, null);
  }

  @Test
  public void doesNotThrow_RecordWithContext() {
    MeasureDouble myMeasure = meter.measureDoubleBuilder("MyMeasure").build();
    myMeasure.getDefaultHandle().record(5.0, distContextManager.getCurrentContext());
  }
}
