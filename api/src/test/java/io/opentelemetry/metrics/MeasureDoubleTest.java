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

import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link MeasureDouble}. */
@RunWith(JUnit4.class)
public final class MeasureDoubleTest {
  private static final Meter meter = DefaultMeter.getInstance();

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void preventTooLongMeasureName() {
    char[] chars = new char[256];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    thrown.expect(IllegalArgumentException.class);
    meter.measureDoubleBuilder(longName).build();
  }

  @Test
  public void preventNonPrintableMeasureName() {
    thrown.expect(IllegalArgumentException.class);
    meter.measureDoubleBuilder("\2").build();
  }

  @Test
  public void preventNull_Description() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("constantLabels");
    meter.measureDoubleBuilder("metric").setDescription(null).build();
  }

  @Test
  public void preventNull_Unit() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("constantLabels");
    meter.measureDoubleBuilder("metric").setUnit(null).build();
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
    myMeasure.record(-5.0);
  }
}
