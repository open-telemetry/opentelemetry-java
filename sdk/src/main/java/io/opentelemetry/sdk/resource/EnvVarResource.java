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

package io.opentelemetry.sdk.resource;

import io.opentelemetry.resource.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Provides a framework for detection of resource information from the environment variable
 * "OC_RESOURCE_LABELS".
 *
 * @since 0.1.0
 */
@ThreadSafe
public final class EnvVarResource {
  private static final String OC_RESOURCE_LABELS_ENV = "OC_RESOURCE_LABELS";
  private static final String LABEL_LIST_SPLITTER = ",";
  private static final String LABEL_KEY_VALUE_SPLITTER = "=";

  private static final Resource ENV_VAR_RESOURCE =
      Resource.create(parseResourceLabels(System.getenv(OC_RESOURCE_LABELS_ENV)));

  private EnvVarResource() {}

  /**
   * Returns a {@link Resource}. This resource information is loaded from the OC_RESOURCE_LABELS
   * environment variable.
   *
   * @return a {@code Resource}.
   * @since 0.1.0
   */
  public static Resource getResource() {
    return ENV_VAR_RESOURCE;
  }

  /*
   * Creates a label map from the OC_RESOURCE_LABELS environment variable.
   *
   * <p>OC_RESOURCE_LABELS: A comma-separated list of labels describing the source in more detail,
   * e.g. “key1=val1,key2=val2”. Domain names and paths are accepted as label keys. Values may be
   * quoted or unquoted in general. If a value contains whitespaces, =, or " characters, it must
   * always be quoted.
   */
  private static Map<String, String> parseResourceLabels(@Nullable String rawEnvLabels) {
    if (rawEnvLabels == null) {
      return Collections.emptyMap();
    } else {
      Map<String, String> labels = new HashMap<>();
      String[] rawLabels = rawEnvLabels.split(LABEL_LIST_SPLITTER, -1);
      for (String rawLabel : rawLabels) {
        String[] keyValuePair = rawLabel.split(LABEL_KEY_VALUE_SPLITTER, -1);
        if (keyValuePair.length != 2) {
          continue;
        }
        String key = keyValuePair[0].trim();
        String value = keyValuePair[1].trim().replaceAll("^\"|\"$", "");
        labels.put(key, value);
      }
      return Collections.unmodifiableMap(labels);
    }
  }
}
