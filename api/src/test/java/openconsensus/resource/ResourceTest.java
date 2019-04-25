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

package openconsensus.resource;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Resource}. */
@RunWith(JUnit4.class)
public class ResourceTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();
  private static final Resource DEFAULT_RESOURCE =
      Resource.create(Collections.<String, String>emptyMap());
  private static final Resource DEFAULT_RESOURCE_1 =
      Resource.create(Collections.singletonMap("a", "100"));
  private Resource resource1;
  private Resource resource2;

  @Before
  public void setUp() {
    Map<String, String> labelMap1 = new HashMap<>();
    labelMap1.put("a", "1");
    labelMap1.put("b", "2");
    Map<String, String> labelMap2 = new HashMap<>();
    labelMap2.put("a", "1");
    labelMap2.put("b", "3");
    labelMap2.put("c", "4");
    resource1 = Resource.create(labelMap1);
    resource2 = Resource.create(labelMap2);
  }

  @Test
  public void create() {
    Map<String, String> labelMap = new HashMap<>();
    labelMap.put("a", "1");
    labelMap.put("b", "2");
    Resource resource = Resource.create(labelMap);
    assertThat(resource.getLabels()).isNotNull();
    assertThat(resource.getLabels().size()).isEqualTo(2);
    assertThat(resource.getLabels()).isEqualTo(labelMap);

    Resource resource1 = Resource.create(Collections.<String, String>emptyMap());
    assertThat(resource1.getLabels()).isNotNull();
    assertThat(resource1.getLabels()).isEmpty();
  }

  @Test
  public void testResourceEquals() {
    Map<String, String> labelMap1 = new HashMap<>();
    labelMap1.put("a", "1");
    labelMap1.put("b", "2");
    Map<String, String> labelMap2 = new HashMap<>();
    labelMap2.put("a", "1");
    labelMap2.put("b", "3");
    labelMap2.put("c", "4");
    new EqualsTester()
        .addEqualityGroup(Resource.create(labelMap1), Resource.create(labelMap1))
        .addEqualityGroup(Resource.create(labelMap2))
        .testEquals();
  }

  @Test
  public void testMergeResources() {
    Map<String, String> expectedLabelMap = new HashMap<>();
    expectedLabelMap.put("a", "1");
    expectedLabelMap.put("b", "2");
    expectedLabelMap.put("c", "4");

    Resource resource = DEFAULT_RESOURCE.merge(resource1).merge(resource2);
    assertThat(resource.getLabels()).isEqualTo(expectedLabelMap);
  }

  @Test
  public void testMergeResources_Resource1() {
    Map<String, String> expectedLabelMap = new HashMap<>();
    expectedLabelMap.put("a", "1");
    expectedLabelMap.put("b", "2");

    Resource resource = DEFAULT_RESOURCE.merge(resource1);
    assertThat(resource.getLabels()).isEqualTo(expectedLabelMap);
  }

  @Test
  public void testMergeResources_Resource1_Null() {
    Map<String, String> expectedLabelMap = new HashMap<>();
    expectedLabelMap.put("a", "1");
    expectedLabelMap.put("b", "3");
    expectedLabelMap.put("c", "4");

    Resource resource = DEFAULT_RESOURCE.merge(null).merge(resource2);
    assertThat(resource.getLabels()).isEqualTo(expectedLabelMap);
  }

  @Test
  public void testMergeResources_Resource2_Null() {
    Map<String, String> expectedLabelMap = new HashMap<>();
    expectedLabelMap.put("a", "1");
    expectedLabelMap.put("b", "2");

    Resource resource = DEFAULT_RESOURCE.merge(resource1).merge(null);
    assertThat(resource.getLabels()).isEqualTo(expectedLabelMap);
  }
}
