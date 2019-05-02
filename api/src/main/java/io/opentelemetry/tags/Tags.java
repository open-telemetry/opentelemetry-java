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

package io.opentelemetry.tags;

/**
 * Class to access the global {@link Tagger}.
 *
 * @since 0.1.0
 */
public final class Tags {
  private static final Tagger TAGGER = NoopTags.newNoopTagger();

  private Tags() {}

  /**
   * Returns the default {@code Tagger}.
   *
   * @return the default {@code Tagger}.
   * @since 0.1.0
   */
  public static Tagger getTagger() {
    return TAGGER;
  }
}
