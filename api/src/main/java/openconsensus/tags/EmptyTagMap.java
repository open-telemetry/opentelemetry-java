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

import java.util.Collections;
import java.util.Iterator;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/** An immutable implementation of the {@link TagMap} that does not contain any tags. */
@Immutable
public class EmptyTagMap extends TagMap {
  private static final Iterator<Tag> EMPTY_ITERATOR = Collections.<Tag>emptyList().iterator();

  /** Returns the single instance of the {@link EmptyTagMap} class. */
  public static final TagMap INSTANCE = new EmptyTagMap();

  @Override
  public Iterator<Tag> getIterator() {
    return EMPTY_ITERATOR;
  }

  @Nullable
  @Override
  public TagValue getTagValue(TagKey tagKey) {
    return null;
  }

  private EmptyTagMap() {}
}
