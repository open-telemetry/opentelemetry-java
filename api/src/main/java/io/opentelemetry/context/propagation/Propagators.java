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

package io.opentelemetry.context.propagation;

public final class Propagators {
  private final HttpInjector injector;
  private final HttpExtractor extractor;

  public static Propagators create(HttpInjector injector, HttpExtractor extractor) {
    return new Propagators(injector, extractor);
  }

  private Propagators(HttpInjector injector, HttpExtractor extractor) {
    this.injector = injector;
    this.extractor = extractor;
  }

  public HttpExtractor getHttpExtractor() {
    return extractor;
  }

  public HttpInjector getHttpInjector() {
    return injector;
  }
}
