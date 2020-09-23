/*
 * Copyright The OpenTelemetry Authors
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
package io.opentelemetry.context;

import io.opentelemetry.context.ThreadLocalContextStorage.NoopScope;

/**
 * An {@link AutoCloseable} that represents a mounted context for a block of code. A failure to call
 * {@link Scope#close()} will generally break tracing or cause memory leaks. It is recommended that
 * you use this class with a {@code try-with-resources} block:
 *
 * <pre>{code
 *   try (Scope ignored = tracer.withSpan(span)) {
 *     ...
 *   }
 * }</pre>
 */
public interface Scope extends AutoCloseable {

  static Scope noop() {
    return NoopScope.INSTANCE;
  }

  @Override
  void close();
}
