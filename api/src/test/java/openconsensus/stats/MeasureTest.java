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

package openconsensus.stats;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Measure}. */
@RunWith(JUnit4.class)
public final class MeasureTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void testConstants() {
    assertThat(Measure.NAME_MAX_LENGTH).isEqualTo(255);
  }

  @Test
  public void preventTooLongMeasureName() {
    char[] chars = new char[Measure.NAME_MAX_LENGTH + 1];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    thrown.expect(IllegalArgumentException.class);
    Measure.MeasureDouble.create(longName, "description", "1");
  }

  @Test
  public void preventNonPrintableMeasureName() {
    thrown.expect(IllegalArgumentException.class);
    Measure.MeasureDouble.create("\2", "description", "1");
  }

  @Test
  public void testMeasureComponents() {
    Measure measurement = Measure.MeasureDouble.create("Foo", "The description of Foo", "Mbit/s");
    assertThat(measurement.getName()).isEqualTo("Foo");
    assertThat(measurement.getDescription()).isEqualTo("The description of Foo");
    assertThat(measurement.getUnit()).isEqualTo("Mbit/s");
  }

  @Test
  public void testMeasureEquals() {
    new EqualsTester()
        .addEqualityGroup(
            Measure.MeasureDouble.create("name", "description", "bit/s"),
            Measure.MeasureDouble.create("name", "description", "bit/s"))
        .addEqualityGroup(Measure.MeasureLong.create("name", "description", "bit/s"))
        .addEqualityGroup(Measure.MeasureDouble.create("name", "description 2", "bit/s"))
        .testEquals();
  }
}
