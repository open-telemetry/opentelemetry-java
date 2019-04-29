/*
 * Copyright 2019, OpenConsensus Authors
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

package openconsensus.metrics;

import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link openconsensus.metrics.Measure}. */
@RunWith(JUnit4.class)
public final class MeasureTest {
  private static final Meter meter = NoopMetrics.newNoopMeter();

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void preventTooLongMeasureName() {
    char[] chars = new char[256];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    thrown.expect(IllegalArgumentException.class);
    meter.measureBuilder(longName).build();
  }

  @Test
  public void preventNonPrintableMeasureName() {
    thrown.expect(IllegalArgumentException.class);
    meter.measureBuilder("\2").build();
  }

  @Test
  public void preventNegativeValue() {
    Measure myMeasure = meter.measureBuilder("MyMeasure").build();
    thrown.expect(IllegalArgumentException.class);
    myMeasure.createDoubleMeasurement(-5);
  }
}
