/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Provides a framework for detection of resource information from the environment variable
 * "OTEL_RESOURCE_ATTRIBUTES" and system properties "otel.resource.attributes".
 */
@ThreadSafe
final class EnvAutodetectResource {
  private static final String ATTRIBUTE_LIST_SPLITTER = ",";
  private static final String ATTRIBUTE_KEY_VALUE_SPLITTER = "=";

  private EnvAutodetectResource() {}

  /*
   * Creates an attribute map from the OTEL_RESOURCE_ATTRIBUTES environment variable or
   * otel.resource.attributes system properties.
   *
   * <p>OTEL_RESOURCE_ATTRIBUTES: A comma-separated list of attributes describing the source in more
   * detail, e.g. “key1=val1,key2=val2”. Domain names and paths are accepted as attribute keys.
   * Values may be quoted or unquoted in general.
   * If a value contains whitespaces, =, or " characters, it must always be quoted.
   */
  // Visible for testing
  static Attributes parseResourceAttributes(@Nullable String rawEnvAttributes) {
    if (rawEnvAttributes == null) {
      return Attributes.empty();
    } else {
      AttributesBuilder attrBuilders = Attributes.builder();
      String[] rawAttributes = rawEnvAttributes.split(ATTRIBUTE_LIST_SPLITTER, -1);
      for (String rawAttribute : rawAttributes) {
        String[] keyValuePair = rawAttribute.split(ATTRIBUTE_KEY_VALUE_SPLITTER, -1);
        if (keyValuePair.length != 2) {
          continue;
        }
        attrBuilders.put(keyValuePair[0].trim(), keyValuePair[1].trim().replaceAll("^\"|\"$", ""));
      }
      return attrBuilders.build();
    }
  }

  /** Builder utility for this EnvAutodetectResource. */
  protected static class Builder extends ConfigBuilder<Builder> {
    private static final String OTEL_RESOURCE_ATTRIBUTES_KEY = "otel.resource.attributes";
    private String envAttributes;

    @Override
    protected Builder fromConfigMap(
        Map<String, String> configMap, NamingConvention namingConvention) {
      configMap = namingConvention.normalize(configMap);
      String envAttributesValue = getStringProperty(OTEL_RESOURCE_ATTRIBUTES_KEY, configMap);
      if (envAttributesValue != null) {
        this.setEnvAttributes(envAttributesValue);
      }
      return this;
    }

    public Builder setEnvAttributes(String envAttributes) {
      this.envAttributes = envAttributes;
      return this;
    }

    public Resource build() {
      return Resource.create(parseResourceAttributes(this.envAttributes));
    }
  }
}
