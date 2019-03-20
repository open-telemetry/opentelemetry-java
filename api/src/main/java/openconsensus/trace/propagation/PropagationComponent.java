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

import openconsensus.common.ExperimentalApi;

/**
 * Container class for all the supported propagation formats. Currently supports only Binary format
 * (see {@link BinaryFormat}) and B3 Text format (see {@link TextFormat}) but more formats will be
 * added.
 *
 * @since 0.1.0
 */
public abstract class PropagationComponent {
  private static final PropagationComponent NOOP_PROPAGATION_COMPONENT =
      new NoopPropagationComponent();

  /**
   * Returns the {@link BinaryFormat} with the provided implementations. If no implementation is
   * provided then no-op implementation will be used.
   *
   * @return the {@code BinaryFormat} implementation.
   * @since 0.1.0
   */
  public abstract BinaryFormat getBinaryFormat();

  /**
   * Returns the B3 {@link TextFormat} with the provided implementations. See <a
   * href="https://github.com/openzipkin/b3-propagation">b3-propagation</a> for more information. If
   * no implementation is provided then no-op implementation will be used.
   *
   * @since 0.1.0
   * @return the B3 {@code TextFormat} implementation.
   */
  @ExperimentalApi
  public abstract TextFormat getB3Format();

  /**
   * Returns the TraceContext {@link TextFormat} with the provided implementations. See <a
   * href="https://github.com/w3c/distributed-tracing">w3c/distributed-tracing</a> for more
   * information. If no implementation is provided then no-op implementation will be used.
   *
   * @since 0.1.0
   * @return the TraceContext {@code TextFormat} implementation.
   */
  @ExperimentalApi
  public abstract TextFormat getTraceContextFormat();

  /**
   * Returns an instance that contains no-op implementations for all the instances.
   *
   * @return an instance that contains no-op implementations for all the instances.
   * @since 0.1.0
   */
  public static PropagationComponent getNoopPropagationComponent() {
    return NOOP_PROPAGATION_COMPONENT;
  }

  private static final class NoopPropagationComponent extends PropagationComponent {
    @Override
    public BinaryFormat getBinaryFormat() {
      return BinaryFormat.getNoopBinaryFormat();
    }

    @Override
    public TextFormat getB3Format() {
      return TextFormat.getNoopTextFormat();
    }

    @Override
    public TextFormat getTraceContextFormat() {
      return TextFormat.getNoopTextFormat();
    }
  }
}
