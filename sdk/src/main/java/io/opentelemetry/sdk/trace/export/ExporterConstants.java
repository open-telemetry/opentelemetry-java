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

package io.opentelemetry.sdk.trace.export;

import java.util.concurrent.TimeUnit;

/** Provides constants for all exporter default configuration values. */
public class ExporterConstants {

  private ExporterConstants() {}

  /** Common default host name. */
  public static final String DEFAULT_HOST_NAME = "unknown";
  /** Common default service name. */
  public static final String DEFAULT_SERVICE_NAME = DEFAULT_HOST_NAME;

  public static final long DEFAULT_DEADLINE_MS = TimeUnit.SECONDS.toMillis(1); // 1 second

  /** Default endpoint for otlp. */
  public static final String OTLP_DEFAULT_ENDPOINT = "localhost:55680";
  /** Default tls flag for otlp. */
  public static final boolean OTLP_DEFAULT_USE_TLS = false;

  /** Default endpoint for jaeger. */
  public static final String JAEGER_DEFAULT_ENDPOINT = "localhost:14250";
  /** Default ip for jaeger. */
  public static final String JAEGER_DEFAULT_IP = "0.0.0.0";

  /** Default endpoint for zipkin. */
  public static final String ZIPKIN_DEFAULT_ENDPOINT = "http://localhost:9411/api/v2/spans";
}
