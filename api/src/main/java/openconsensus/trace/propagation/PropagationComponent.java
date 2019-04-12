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

package openconsensus.trace.propagation;

/**
 * Container class for all the supported propagation formats. Currently supports only Binary format
 * (see {@link BinaryFormat}) and B3 Text format (see {@link TextFormat}) but more formats will be
 * added.
 *
 * @since 0.1.0
 */
public abstract class PropagationComponent {
  /**
   * Returns the {@link BinaryFormat} for this implementation.
   *
   * <p>If no implementation is provided then no-op implementation will be used.
   *
   * <p>Usually this will be the W3C Trace Context as the binary format. For more details,
   * see <a href="https://github.com/w3c/trace-context">trace-context</a>.
   *
   * @return the {@code BinaryFormat} for this implementation.
   * @since 0.1.0
   */
  public abstract BinaryFormat getBinaryFormat();

  /**
   * Returns the {@link TextFormat} for this implementation.
   *
   * <p>If no implementation is provided then no-op implementation will be used.
   *
   * <p>Usually this will be the W3C Trace Context as the HTTP text format. For more details,
   * see <a href="https://github.com/w3c/trace-context">trace-context</a>.
   *
   * @return the {@code TextFormat} for this implementation.
   * @since 0.1.0
   */
  public abstract TextFormat getTextFormat();
}
