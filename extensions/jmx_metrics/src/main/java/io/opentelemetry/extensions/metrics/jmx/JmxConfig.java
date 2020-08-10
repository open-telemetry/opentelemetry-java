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

package io.opentelemetry.extensions.metrics.jmx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JmxConfig {
  public String serviceUrl;
  public String groovyScript;

  public int intervalSeconds;
  public String exporterType;
  public String exporterEndpoint;

  public String username;
  public String password;

  public String keyStorePath;
  public String keyStorePassword;
  public String keyStoreType;
  public String trustStorePath;
  public String trustStorePassword;
  public String jmxRemoteProfiles;
  public String realm;

  /**
   * Will determine if parsed config is complete, setting any applicable defaults.
   *
   * @throws ConfigureError - Thrown if a configuration value is missing or invalid.
   */
  public void validate() throws ConfigureError {
    if (isBlank(this.serviceUrl)) {
      throw new ConfigureError("serviceUrl must be specified.");
    }

    if (isBlank(this.groovyScript)) {
      throw new ConfigureError("groovyScript must be specified.");
    }

    if (isBlank(this.exporterType)) {
      this.exporterType = "logging";
    }

    if (isBlank(this.exporterEndpoint) && this.exporterType.equalsIgnoreCase("otlp")) {
      throw new ConfigureError("exporterEndpoint must be specified for otlp format.");
    }

    if (this.intervalSeconds < 0) {
      throw new ConfigureError("intervalSeconds must be positive.");
    }

    if (this.intervalSeconds == 0) {
      this.intervalSeconds = 10;
    }
  }

  /**
   * Determines if a String is null or without non-whitespace chars.
   *
   * @param s - {@link String} to evaluate
   * @return - if s is null or without non-whitespace chars.
   */
  public static boolean isBlank(String s) {
    if (s == null) {
      return true;
    }
    return s.trim().length() == 0;
  }
}
