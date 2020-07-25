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

package io.opentelemetry.sdk.resources;

import static com.google.common.truth.Truth.assertThat;
import static io.opentelemetry.common.AttributeValue.stringAttributeValue;

import io.opentelemetry.common.Attributes;
import org.junit.BeforeClass;
import org.junit.Test;

/** Tests for the {@link EnvVarResource}. */
public class EnvVarResourceTest {

  @Test
  public void parseResourceAttributes_null() {
    assertThat(EnvVarResource.parseResourceAttributes(null).isEmpty()).isTrue();
  }

  @Test
  public void parseResourceAttributes_empty() {
    assertThat(EnvVarResource.parseResourceAttributes("").isEmpty()).isTrue();
  }

  @Test
  public void parseResourceAttributes_malformed() {
    assertThat(EnvVarResource.parseResourceAttributes("value/foo").isEmpty()).isTrue();
  }

  @Test
  public void parseResourceAttributes_single() {
    Attributes result = EnvVarResource.parseResourceAttributes("value=foo");
    assertThat(result).isEqualTo(Attributes.of("value", stringAttributeValue("foo")));
  }

  @Test
  public void parseResourceAttributes_multi() {
    Attributes result = EnvVarResource.parseResourceAttributes("value=foo, other=bar");
    assertThat(result)
        .isEqualTo(
            Attributes.of(
                "value", stringAttributeValue("foo"),
                "other", stringAttributeValue("bar")));
  }

  @Test
  public void parseResourceAttributes_whitespace() {
    Attributes result = EnvVarResource.parseResourceAttributes(" value = foo ");
    assertThat(result).isEqualTo(Attributes.of("value", stringAttributeValue("foo")));
  }

  @Test
  public void parseResourceAttributes_quotes() {
    Attributes result = EnvVarResource.parseResourceAttributes("value=\"foo\"");
    assertThat(result).isEqualTo(Attributes.of("value", stringAttributeValue("foo")));
  }

  @BeforeClass
  public static void init() {
    System.setProperty("otel.resource.attributes", "value = foo");
  }

  @Test
  public void getResourceAttributes_properties() {
    Attributes result = (Attributes) EnvVarResource.getResource().getAttributes();
    assertThat(result).isEqualTo(Attributes.of("value", stringAttributeValue("foo")));
  }
}
