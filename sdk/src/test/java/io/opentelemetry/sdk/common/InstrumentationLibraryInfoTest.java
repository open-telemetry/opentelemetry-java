/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.sdk.common;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link InstrumentationLibraryInfo}. */
@RunWith(JUnit4.class)
public class InstrumentationLibraryInfoTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void emptyLibraryInfo() {
    assertThat(InstrumentationLibraryInfo.getEmpty().getName()).isEmpty();
    assertThat(InstrumentationLibraryInfo.getEmpty().getVersion()).isNull();
  }

  @Test
  public void nullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    InstrumentationLibraryInfo.create(null, "semver:1.0.0");
  }
}
