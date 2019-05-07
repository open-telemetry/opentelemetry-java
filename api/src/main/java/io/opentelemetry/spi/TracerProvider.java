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

package io.opentelemetry.spi;

import io.opentelemetry.trace.Tracer;

/**
 * TracerProvider is a service provider for {@link Tracer}. Implementation should register
 * implementation in <code>META-INF/services/io.opentelemetry.spi.TracerProvider</code>. <br>
 * <br>
 * A specific implementation can be specified by a system property {@code
 * io.opentelemetry.spi.TracerProvider} with value of fully qualified class name.
 */
public interface TracerProvider {

  /**
   * Creates a new tracer instance.
   *
   * @return a tracer instance.
   */
  Tracer create();
}
