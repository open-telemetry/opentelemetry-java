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

package io.opentelemetry.sdk.internal;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link InstrumentationLibraryInfo}. */
@RunWith(JUnit4.class)
public class ComponentRegistryTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final String INSTRUMENTATION_NAME = "test_name";
  private static final String INSTRUMENTATION_VERSION = "version";
  private final ComponentRegistry<TestComponent> registry =
      new ComponentRegistry<TestComponent>() {
        @Override
        public TestComponent newComponent(InstrumentationLibraryInfo instrumentationLibraryInfo) {
          return new TestComponent(instrumentationLibraryInfo);
        }
      };

  @Test
  public void libraryName_MustNotBeNull() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    registry.get(null, "version");
  }

  @Test
  public void libraryVersion_AllowsNull() {
    TestComponent testComponent = registry.get(INSTRUMENTATION_NAME, null);
    assertThat(testComponent).isNotNull();
    assertThat(testComponent.instrumentationLibraryInfo.getName()).isEqualTo(INSTRUMENTATION_NAME);
    assertThat(testComponent.instrumentationLibraryInfo.getVersion()).isNull();
  }

  @Test
  public void getSameInstanceForSameName_WithoutVersion() {
    assertThat(registry.get(INSTRUMENTATION_NAME))
        .isSameInstanceAs(registry.get(INSTRUMENTATION_NAME));
    assertThat(registry.get(INSTRUMENTATION_NAME))
        .isSameInstanceAs(registry.get(INSTRUMENTATION_NAME, null));
  }

  @Test
  public void getSameInstanceForSameName_WithVersion() {
    assertThat(registry.get(INSTRUMENTATION_NAME, INSTRUMENTATION_VERSION))
        .isSameInstanceAs(registry.get(INSTRUMENTATION_NAME, INSTRUMENTATION_VERSION));
  }

  @Test
  public void getDifferentInstancesForDifferentNames() {
    assertThat(registry.get(INSTRUMENTATION_NAME, INSTRUMENTATION_VERSION))
        .isNotSameInstanceAs(registry.get(INSTRUMENTATION_NAME + "_2", INSTRUMENTATION_VERSION));
  }

  @Test
  public void getDifferentInstancesForDifferentVersions() {
    assertThat(registry.get(INSTRUMENTATION_NAME, INSTRUMENTATION_VERSION))
        .isNotSameInstanceAs(registry.get(INSTRUMENTATION_NAME, INSTRUMENTATION_VERSION + "_1"));
  }

  private static final class TestComponent {
    private final InstrumentationLibraryInfo instrumentationLibraryInfo;

    private TestComponent(InstrumentationLibraryInfo instrumentationLibraryInfo) {
      this.instrumentationLibraryInfo = instrumentationLibraryInfo;
    }
  }
}
