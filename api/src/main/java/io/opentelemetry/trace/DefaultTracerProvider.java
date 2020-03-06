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

package io.opentelemetry.trace;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class DefaultTracerProvider implements TracerProvider {

  private static final TracerProvider instance = new DefaultTracerProvider();

  /**
   * Returns a {@code TracerProvider} singleton that is the default implementation for {@link
   * TracerProvider}.
   *
   * @return a {@code TracerProvider} singleton that is the default implementation for {@link
   *     TracerProvider}.
   */
  public static TracerProvider getInstance() {
    return instance;
  }

  @Override
  public Tracer get(String instrumentationName) {
    return get(instrumentationName, null);
  }

  @Override
  public Tracer get(String instrumentationName, String instrumentationVersion) {
    return DefaultTracer.getInstance();
  }

  private DefaultTracerProvider() {}
}
