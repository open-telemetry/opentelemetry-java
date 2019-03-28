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

package openconsensus.tags;

import javax.annotation.concurrent.Immutable;

/**
 * A map from {@link TagKey} to {@link TagValue} that can be used to label anything that is
 * associated with a specific operation.
 *
 * <p>For example, {@code TagMap}s can be used to label stats, log messages, or debugging
 * information.
 *
 * @since 0.1.0
 */
@Immutable
public abstract class TagMap {
  @Override
  public String toString() {
    return "TagMap";
  }
}
