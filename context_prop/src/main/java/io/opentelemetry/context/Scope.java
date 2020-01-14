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

package io.opentelemetry.context;

import java.io.Closeable;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * A {@link java.io.Closeable} that represents a change to the current context over a scope of code.
 * {@link Scope#close} cannot throw a checked exception.
 *
 * <p>Example of usage:
 *
 * <pre>
 *   try (Scope ctx = tracer.withSpan(span)) {
 *     ...
 *   }
 * </pre>
 *
 * @since 0.1.0
 */
@NotThreadSafe
public interface Scope extends Closeable {
  @Override
  void close();
}
