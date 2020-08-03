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

import static io.opentelemetry.common.AttributeValue.stringAttributeValue;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.common.Attributes;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

/** Tests for the {@link EnvAutodetectResource}. */
class EnvAutodetectResourceTest {

  @Test
  void parseResourceAttributes_null() {
    assertThat(EnvAutodetectResource.parseResourceAttributes(null).isEmpty()).isTrue();
  }

  @Test
  void parseResourceAttributes_empty() {
    assertThat(EnvAutodetectResource.parseResourceAttributes("").isEmpty()).isTrue();
  }

  @Test
  void parseResourceAttributes_malformed() {
    assertThat(EnvAutodetectResource.parseResourceAttributes("value/foo").isEmpty()).isTrue();
  }

  @Test
  void parseResourceAttributes_single() {
    Attributes result = EnvAutodetectResource.parseResourceAttributes("value=foo");
    assertThat(result).isEqualTo(Attributes.of("value", stringAttributeValue("foo")));
  }

  @Test
  void parseResourceAttributes_multi() {
    Attributes result = EnvAutodetectResource.parseResourceAttributes("value=foo, other=bar");
    assertThat(result)
        .isEqualTo(
            Attributes.of(
                "value", stringAttributeValue("foo"),
                "other", stringAttributeValue("bar")));
  }

  @Test
  void parseResourceAttributes_whitespace() {
    Attributes result = EnvAutodetectResource.parseResourceAttributes(" value = foo ");
    assertThat(result).isEqualTo(Attributes.of("value", stringAttributeValue("foo")));
  }

  @Test
  void parseResourceAttributes_quotes() {
    Attributes result = EnvAutodetectResource.parseResourceAttributes("value=\"foo\"");
    assertThat(result).isEqualTo(Attributes.of("value", stringAttributeValue("foo")));
  }

  @Test
  void getResourceAttributes_properties() {
    String key = "otel.resource.attributes";
    System.setProperty(key, "value = foo");
    Resource resource =
        new EnvAutodetectResource.Builder()
            .readEnvironmentVariables()
            .readSystemProperties()
            .build();
    Attributes result = (Attributes) resource.getAttributes();
    assertThat(result).isEqualTo(Attributes.of("value", stringAttributeValue("foo")));
    System.clearProperty(key);
  }

  public static class ResourceAttributesEnvVarsTest {

    @Test
    @SetEnvironmentVariable(key = "OTEL_RESOURCE_ATTRIBUTES", value = "value = foo")
    public void getResourceAttributes_envvars() {
      Resource resource =
          new EnvAutodetectResource.Builder()
              .readEnvironmentVariables()
              .readSystemProperties()
              .build();
      Attributes result = (Attributes) resource.getAttributes();
      assertThat(result).isEqualTo(Attributes.of("value", stringAttributeValue("foo")));
    }
  }
}
