/*
 * Copyright 2019, OpenTelemetry Authors
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

import io.opentelemetry.common.AttributeValue;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Provides a framework for detection of resource information from the environment variable
 * "OTEL_RESOURCE_ATTRIBUTES".
 *
 * @since 0.1.0
 */
@ThreadSafe
public final class EnvVarResource {
  private static final String OTEL_RESOURCE_ATTRIBUTES_ENV = "OTEL_RESOURCE_ATTRIBUTES";
  private static final String ATTRIBUTE_LIST_SPLITTER = ",";
  private static final String ATTRIBUTE_KEY_VALUE_SPLITTER = "=";

  private static final Resource ENV_VAR_RESOURCE =
      Resource.create(parseResourceAttributes(System.getenv(OTEL_RESOURCE_ATTRIBUTES_ENV)));

  private EnvVarResource() {}

  /**
   * Returns a {@link Resource}. This resource information is loaded from the
   * OTEL_RESOURCE_ATTRIBUTES environment variable.
   *
   * @return a {@code Resource}.
   * @since 0.1.0
   */
  public static Resource getResource() {
    return ENV_VAR_RESOURCE;
  }

  /*
   * Creates an attribute map from the OTEL_RESOURCE_ATTRIBUTES environment variable.
   *
   * <p>OTEL_RESOURCE_ATTRIBUTES: A comma-separated list of attributes describing the source in more
   * detail, e.g. “key1=val1,key2=val2”. Domain names and paths are accepted as attribute keys.
   * Values may be quoted or unquoted in general.
   * If a value contains whitespaces, =, or " characters, it must always be quoted.
   */
  private static Map<String, AttributeValue> parseResourceAttributes(
      @Nullable String rawEnvAttributes) {
    if (rawEnvAttributes == null) {
      return Collections.emptyMap();
    } else {
      Map<String, AttributeValue> attributes = new HashMap<>();
      String[] rawAttributes = rawEnvAttributes.split(ATTRIBUTE_LIST_SPLITTER, -1);
      for (String rawAttribute : rawAttributes) {
        String[] keyValuePair = rawAttribute.split(ATTRIBUTE_KEY_VALUE_SPLITTER, -1);
        if (keyValuePair.length != 2) {
          continue;
        }
        String key = keyValuePair[0].trim();
        AttributeValue value =
            AttributeValue.stringAttributeValue(keyValuePair[1].trim().replaceAll("^\"|\"$", ""));
        attributes.put(key, value);
      }
      return Collections.unmodifiableMap(attributes);
    }
  }
}
