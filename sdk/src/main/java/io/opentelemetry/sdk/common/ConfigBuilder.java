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

package io.opentelemetry.sdk.common;

import java.util.Map;

public abstract class ConfigBuilder {

  protected ConfigBuilder() {}

  public abstract ConfigBuilder fromConfigMap(Map<String, String> configMap);

  public abstract ConfigBuilder fromEnv();

  protected boolean getProperty(String name, Map<String, String> map, boolean defaultValue) {
    try {
      return Boolean.parseBoolean(System.getProperty(name, map.get(name)));
    } catch (NumberFormatException ex) {
      return defaultValue;
    }
  }

  protected int getProperty(String name, Map<String, String> map, int defaultValue) {
    try {
      return Integer.parseInt(System.getProperty(name, map.get(name)));
    } catch (NumberFormatException ex) {
      return defaultValue;
    }
  }

  protected long getProperty(String name, Map<String, String> map, long defaultValue) {
    try {
      return Long.parseLong(System.getProperty(name, map.get(name)));
    } catch (NumberFormatException ex) {
      return defaultValue;
    }
  }
}
