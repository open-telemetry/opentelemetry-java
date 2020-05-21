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

package io.opentelemetry.sdk.common.export;

import static com.google.common.truth.Truth.assertThat;

import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link io.opentelemetry.sdk.common.export.ConfigBuilder}. */
@RunWith(JUnit4.class)
public class ConfigBuilderTest {

  @Test
  public void booleanProperty() {
    Boolean booleanProperty =
        ConfigBuilder.getBooleanProperty("boolean", Collections.singletonMap("boolean", "true"));
    assertThat(booleanProperty).isTrue();
  }

  @Test
  public void longProperty() {
    Long longProperty =
        ConfigBuilder.getLongProperty("long", Collections.singletonMap("long", "42343"));
    assertThat(longProperty).isEqualTo(42343);
  }

  @Test
  public void intProperty() {
    Integer intProperty =
        ConfigBuilder.getIntProperty("int", Collections.singletonMap("int", "43543"));
    assertThat(intProperty).isEqualTo(43543);
  }

  @Test
  public void doubleProperty() {
    Double doubleProperty =
        ConfigBuilder.getDoubleProperty("double", Collections.singletonMap("double", "5.6"));
    assertThat(doubleProperty).isEqualTo(5.6);
  }

  @Test
  public void invalidBooleanProperty() {
    Boolean booleanProperty =
        ConfigBuilder.getBooleanProperty("boolean", Collections.singletonMap("boolean", "23435"));
    assertThat(booleanProperty).isFalse();
  }

  @Test
  public void invalidLongProperty() {
    Long longProperty =
        ConfigBuilder.getLongProperty("long", Collections.singletonMap("long", "45.6"));
    assertThat(longProperty).isNull();
  }

  @Test
  public void invalidIntProperty() {
    Integer intProperty =
        ConfigBuilder.getIntProperty("int", Collections.singletonMap("int", "false"));
    assertThat(intProperty).isNull();
  }

  @Test
  public void invalidDoubleProperty() {
    Double doubleProperty =
        ConfigBuilder.getDoubleProperty("double", Collections.singletonMap("double", "something"));
    assertThat(doubleProperty).isNull();
  }

  @Test
  public void nullValue_BooleanProperty() {
    Boolean booleanProperty =
        ConfigBuilder.getBooleanProperty("boolean", Collections.<String, String>emptyMap());
    assertThat(booleanProperty).isNull();
  }

  @Test
  public void nullValue_LongProperty() {
    Long longProperty =
        ConfigBuilder.getLongProperty("long", Collections.<String, String>emptyMap());
    assertThat(longProperty).isNull();
  }

  @Test
  public void nullValue_IntProperty() {
    Integer intProperty =
        ConfigBuilder.getIntProperty("int", Collections.<String, String>emptyMap());
    assertThat(intProperty).isNull();
  }

  @Test
  public void nullValue_DoubleProperty() {
    Double doubleProperty =
        ConfigBuilder.getDoubleProperty("double", Collections.<String, String>emptyMap());
    assertThat(doubleProperty).isNull();
  }
}
