/*
 * Copyright 2019, OpenConsensus Authors
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

package openconsensus.common;

import java.io.Closeable;

/**
 * An {@link Closeable} which cannot throw a checked exception.
 *
 * <p>This is useful because such a reversion otherwise requires the caller to catch the
 * (impossible) Exception in the try-with-resources.
 *
 * <p>Example of usage:
 *
 * <pre>
 *   try (NonThrowingAutoCloseable ctx = tryEnter()) {
 *     ...
 *   }
 * </pre>
 *
 * @deprecated {@link Scope} is a better match for operations involving the current context.
 * @since 0.1.0
 */
@Deprecated
public interface NonThrowingCloseable extends Closeable {
  @Override
  void close();
}
