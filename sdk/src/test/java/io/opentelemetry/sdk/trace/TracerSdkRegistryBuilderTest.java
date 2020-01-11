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

package io.opentelemetry.sdk.trace;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TracerSdkRegistryBuilderTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void testHappyPath() {
    TracerSdkRegistry registry =
        TracerSdkRegistry.builder()
            .setClock(mock(Clock.class))
            .setResource(mock(Resource.class))
            .setIdsGenerator(mock(IdsGenerator.class))
            .build();
    assertNotNull(registry);
  }

  @Test
  public void testNullClock() {
    thrown.expect(NullPointerException.class);
    TracerSdkRegistry.builder().setClock(null);
  }

  @Test
  public void testNullResource() {
    thrown.expect(NullPointerException.class);
    TracerSdkRegistry.builder().setResource(null);
  }

  @Test
  public void testNullIdsGenerator() {
    thrown.expect(NullPointerException.class);
    TracerSdkRegistry.builder().setIdsGenerator(null);
  }
}
