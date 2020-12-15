/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
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
    assertThat(result).isEqualTo(Attributes.of(stringKey("value"), "foo"));
  }

  @Test
  void parseResourceAttributes_multi() {
    Attributes result = EnvAutodetectResource.parseResourceAttributes("value=foo, other=bar");
    assertThat(result)
        .isEqualTo(
            Attributes.of(
                stringKey("value"), "foo",
                stringKey("other"), "bar"));
  }

  @Test
  void parseResourceAttributes_whitespace() {
    Attributes result = EnvAutodetectResource.parseResourceAttributes(" value = foo ");
    assertThat(result).isEqualTo(Attributes.of(stringKey("value"), "foo"));
  }

  @Test
  void parseResourceAttributes_quotes() {
    Attributes result = EnvAutodetectResource.parseResourceAttributes("value=\"foo\"");
    assertThat(result).isEqualTo(Attributes.of(stringKey("value"), "foo"));
  }

  @Test
  void getResourceAttributes_properties() {
    String key = "otel.resource.attributes";
    System.setProperty(key, "value = foo,service.name=myservice.name");
    Resource resource =
        new EnvAutodetectResource.Builder()
            .readEnvironmentVariables()
            .readSystemProperties()
            .build();
    Attributes result = resource.getAttributes();
    assertThat(result)
        .isEqualTo(
            Attributes.of(
                stringKey("value"), "foo", ResourceAttributes.SERVICE_NAME, "myservice.name"));
    System.clearProperty(key);
  }

  public static class ResourceAttributesEnvVarsTest {

    @Test
    @SetEnvironmentVariable(
        key = "OTEL_RESOURCE_ATTRIBUTES",
        value = "value = foo,service.name=myservice.name")
    public void getResourceAttributes_envvars() {
      Resource resource =
          new EnvAutodetectResource.Builder()
              .readEnvironmentVariables()
              .readSystemProperties()
              .build();
      Attributes result = resource.getAttributes();
      assertThat(result)
          .isEqualTo(
              Attributes.of(
                  stringKey("value"), "foo", ResourceAttributes.SERVICE_NAME, "myservice.name"));
    }
  }
}
