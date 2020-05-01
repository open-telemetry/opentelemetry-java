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

/** Tests for {@link DoubleMeasure}. */
@RunWith(JUnit4.class)
public final class DoubleMeasureTest {
  private static final Meter meter = DefaultMeter.getInstance();
  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void preventNonPrintableName() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
    meter.doubleMeasureBuilder("\2").build();
  }

  @Test
  public void preventTooLongName() {
    char[] chars = new char[256];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
    meter.doubleMeasureBuilder(longName).build();
  }

  @Test
  public void preventNull_Description() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("description");
    meter.doubleMeasureBuilder("metric").setDescription(null).build();
  }

  @Test
  public void preventNull_Unit() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("unit");
    meter.doubleMeasureBuilder("metric").setUnit(null).build();
  }

  @Test
  public void preventNull_ConstantLabels() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("constantLabels");
    meter.doubleMeasureBuilder("metric").setConstantLabels(null).build();
  }

  @Test
  public void preventNegativeValue() {
    DoubleMeasure myMeasure = meter.doubleMeasureBuilder("MyMeasure").build();
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Unsupported negative values");
    myMeasure.bind().record(-5.0);
  }

  @Test
  public void doesNotThrow() {
    DoubleMeasure myMeasure = meter.doubleMeasureBuilder("MyMeasure").build();
    myMeasure.bind().record(5.0);
  }
}
