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

package openconsensus.trace;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import openconsensus.trace.data.Status;

/**
 * A class that enables overriding the default values used when ending a {@link Span}. Allows
 * overriding the {@link Status status}.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class EndSpanOptions {
  /**
   * The default {@code EndSpanOptions}.
   *
   * @since 0.1.0
   */
  public static final EndSpanOptions DEFAULT = builder().build();

  /**
   * Returns a new {@link Builder} with default options.
   *
   * @return a new {@code Builder} with default options.
   * @since 0.1.0
   */
  public static Builder builder() {
    return new AutoValue_EndSpanOptions.Builder();
  }

  /**
   * Returns the status.
   *
   * <p>If {@code null} then the {@link Span} will record the {@link Status} set via {@link
   * Span#setStatus(Status)} or the default {@link Status#OK} if no status was set.
   *
   * @return the status.
   * @since 0.1.0
   */
  @Nullable
  public abstract Status getStatus();

  /**
   * Builder class for {@link EndSpanOptions}.
   *
   * @since 0.1.0
   */
  @AutoValue.Builder
  public abstract static class Builder {
    /**
     * Sets the status for the {@link Span}.
     *
     * <p>If set, this will override the status set via {@link Span#setStatus(Status)}.
     *
     * @param status the status.
     * @return this.
     * @since 0.1.0
     */
    public abstract Builder setStatus(Status status);

    /**
     * Builds and returns a {@code EndSpanOptions} with the desired settings.
     *
     * @return a {@code EndSpanOptions} with the desired settings.
     * @since 0.1.0
     */
    public abstract EndSpanOptions build();

    Builder() {}
  }

  EndSpanOptions() {}
}
