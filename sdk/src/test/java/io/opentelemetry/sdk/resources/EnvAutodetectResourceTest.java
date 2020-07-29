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
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for the {@link EnvAutodetectResource}. */
public class EnvAutodetectResourceTest {

  @Test
  public void parseResourceAttributes_null() {
    assertThat(EnvAutodetectResource.parseResourceAttributes(null).isEmpty()).isTrue();
  }

  @Test
  public void parseResourceAttributes_empty() {
    assertThat(EnvAutodetectResource.parseResourceAttributes("").isEmpty()).isTrue();
  }

  @Test
  public void parseResourceAttributes_malformed() {
    assertThat(EnvAutodetectResource.parseResourceAttributes("value/foo").isEmpty()).isTrue();
  }

  @Test
  public void parseResourceAttributes_single() {
    Attributes result = EnvAutodetectResource.parseResourceAttributes("value=foo");
    assertThat(result).isEqualTo(Attributes.of("value", stringAttributeValue("foo")));
  }

  @Test
  public void parseResourceAttributes_multi() {
    Attributes result = EnvAutodetectResource.parseResourceAttributes("value=foo, other=bar");
    assertThat(result)
        .isEqualTo(
            Attributes.of(
                "value", stringAttributeValue("foo"),
                "other", stringAttributeValue("bar")));
  }

  @Test
  public void parseResourceAttributes_whitespace() {
    Attributes result = EnvAutodetectResource.parseResourceAttributes(" value = foo ");
    assertThat(result).isEqualTo(Attributes.of("value", stringAttributeValue("foo")));
  }

  @Test
  public void parseResourceAttributes_quotes() {
    Attributes result = EnvAutodetectResource.parseResourceAttributes("value=\"foo\"");
    assertThat(result).isEqualTo(Attributes.of("value", stringAttributeValue("foo")));
  }

  @Test
  public void getResourceAttributes_properties() {
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

  @RunWith(JUnit4.class)
  public static class ResourceAttributesEnvVarsTest {
    @Rule
    public final EnvironmentVariables environmentVariables =
        new EnvironmentVariables().set("OTEL_RESOURCE_ATTRIBUTES", "value = foo");

    @Test
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
