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

package io.opentelemetry.exporters.otlp;

public class CommonProperties {
  public static final String KEY_TIMEOUT = "otel.exporter.otlp.timeout";
  public static final String KEY_ENDPOINT = "otel.exporter.otlp.endpoint";
  public static final String KEY_USE_TLS = "otel.exporter.otlp.insecure";
  public static final String KEY_HEADERS = "otel.exporter.otlp.headers";

  private CommonProperties() {}
}
