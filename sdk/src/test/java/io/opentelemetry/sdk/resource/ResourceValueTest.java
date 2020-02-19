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

package io.opentelemetry.sdk.resource;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceValue;
import java.util.HashMap;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Resource}. */
@RunWith(JUnit4.class)
public class ResourceValueTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void createMultipleTypes() {
    Map<String, ResourceValue> labelMap = new HashMap<>();
    labelMap.put("a", ResourceValue.create("1"));
    labelMap.put("b", ResourceValue.create(2));
    labelMap.put("c", ResourceValue.create(1.23456));
    labelMap.put("d", ResourceValue.create(true));
    Resource resource = Resource.create(labelMap);

    assertThat(resource.getLabels()).isNotNull();
    assertThat(resource.getLabels().size()).isEqualTo(4);
    assertThat(resource.getLabels()).isEqualTo(labelMap);

    assertThat(resource.getLabels().get("a").getType()).isEqualTo(ResourceValue.Type.STRING);
    assertThat(resource.getLabels().get("b").getType()).isEqualTo(ResourceValue.Type.LONG);
    assertThat(resource.getLabels().get("c").getType()).isEqualTo(ResourceValue.Type.DOUBLE);
    assertThat(resource.getLabels().get("d").getType()).isEqualTo(ResourceValue.Type.BOOLEAN);

    assertThat(resource.getLabels().get("a").getStringValue()).isEqualTo("1");
    assertThat(resource.getLabels().get("b").getLongValue()).isEqualTo(2);
    assertThat(resource.getLabels().get("c").getDoubleValue()).isEqualTo(1.23456);
    assertThat(resource.getLabels().get("d").getBooleanValue()).isEqualTo(true);
  }

  @Test
  public void exceptionWrongTypeBoolean_Long() {
    ResourceValue resourceValue = ResourceValue.create(true);
    thrown.expect(UnsupportedOperationException.class);
    resourceValue.getLongValue();
  }

  @Test
  public void exceptionWrongTypeBoolean_Double() {
    ResourceValue resourceValue = ResourceValue.create(true);
    thrown.expect(UnsupportedOperationException.class);
    resourceValue.getDoubleValue();
  }

  @Test
  public void exceptionWrongTypeBoolean_String() {
    ResourceValue resourceValue = ResourceValue.create(true);
    thrown.expect(UnsupportedOperationException.class);
    resourceValue.getStringValue();
  }

  @Test
  public void exceptionWrongTypeLong_Boolean() {
    ResourceValue resourceValue = ResourceValue.create(12345L);
    thrown.expect(UnsupportedOperationException.class);
    resourceValue.getBooleanValue();
  }

  @Test
  public void exceptionWrongTypeLong_Double() {
    ResourceValue resourceValue = ResourceValue.create(12345L);
    thrown.expect(UnsupportedOperationException.class);
    resourceValue.getDoubleValue();
  }

  @Test
  public void exceptionWrongTypeLong_String() {
    ResourceValue resourceValue = ResourceValue.create(12345L);
    thrown.expect(UnsupportedOperationException.class);
    resourceValue.getStringValue();
  }

  @Test
  public void exceptionWrongTypeDouble_Boolean() {
    ResourceValue resourceValue = ResourceValue.create(1.2345);
    thrown.expect(UnsupportedOperationException.class);
    resourceValue.getBooleanValue();
  }

  @Test
  public void exceptionWrongTypeDouble_Long() {
    ResourceValue resourceValue = ResourceValue.create(1.2345);
    thrown.expect(UnsupportedOperationException.class);
    resourceValue.getLongValue();
  }

  @Test
  public void exceptionWrongTypeDouble_String() {
    ResourceValue resourceValue = ResourceValue.create(1.2345);
    thrown.expect(UnsupportedOperationException.class);
    resourceValue.getStringValue();
  }

  @Test
  public void exceptionWrongTypeString_Boolean() {
    ResourceValue resourceValue = ResourceValue.create("1.2345");
    thrown.expect(UnsupportedOperationException.class);
    resourceValue.getBooleanValue();
  }

  @Test
  public void exceptionWrongTypeString_Long() {
    ResourceValue resourceValue = ResourceValue.create("1.2345");
    thrown.expect(UnsupportedOperationException.class);
    resourceValue.getLongValue();
  }

  @Test
  public void exceptionWrongTypeString_Double() {
    ResourceValue resourceValue = ResourceValue.create("1.2345");
    thrown.expect(UnsupportedOperationException.class);
    resourceValue.getDoubleValue();
  }
}
