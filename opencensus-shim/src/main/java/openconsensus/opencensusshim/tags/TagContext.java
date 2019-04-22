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

package openconsensus.opencensusshim.tags;

import java.util.HashMap;
import java.util.Iterator;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A map from {@link TagKey} to {@link TagValue} that can be used to label anything that is
 * associated with a specific operation.
 *
 * <p>For example, {@code TagContext}s can be used to label stats, log messages, or debugging
 * information.
 *
 * @since 0.1.0
 */
@Immutable
public abstract class TagContext {

  /**
   * Returns an iterator over the tags in this {@code TagContext}.
   *
   * @return an iterator over the tags in this {@code TagContext}.
   * @since 0.1.0
   */
  // This method is protected to prevent client code from accessing the tags of any TagContext. We
  // don't currently support efficient access to tags. However, every TagContext subclass needs to
  // provide access to its tags to the stats and tagging implementations by implementing this
  // method. If we decide to support access to tags in the future, we can add a public iterator()
  // method and implement it for all subclasses by calling getIterator().
  //
  // The stats and tagging implementations can access any TagContext's tags through
  // openconsensus.opencensusshim.tags.InternalUtils.getTags, which calls this method.
  protected abstract Iterator<Tag> getIterator();

  @Override
  public String toString() {
    return "TagContext";
  }

  /**
   * Returns true iff the other object is an instance of {@code TagContext} and contains the same
   * key-value pairs. Implementations are free to override this method to provide better
   * performance.
   */
  @Override
  public boolean equals(@Nullable Object other) {
    if (!(other instanceof TagContext)) {
      return false;
    }
    TagContext otherTags = (TagContext) other;
    Iterator<Tag> iter1 = getIterator();
    Iterator<Tag> iter2 = otherTags.getIterator();
    HashMap<Tag, Integer> tags = new HashMap<Tag, Integer>();
    while (iter1 != null && iter1.hasNext()) {
      Tag tag = iter1.next();
      if (tags.containsKey(tag)) {
        tags.put(tag, tags.get(tag) + 1);
      } else {
        tags.put(tag, 1);
      }
    }
    while (iter2 != null && iter2.hasNext()) {
      Tag tag = iter2.next();
      if (!tags.containsKey(tag)) {
        return false;
      }
      int count = tags.get(tag);
      if (count > 1) {
        tags.put(tag, count - 1);
      } else {
        tags.remove(tag);
      }
    }
    return tags.isEmpty();
  }

  @Override
  public final int hashCode() {
    int hashCode = 0;
    Iterator<Tag> i = getIterator();
    if (i == null) {
      return hashCode;
    }
    while (i.hasNext()) {
      Tag tag = i.next();
      if (tag != null) {
        hashCode += tag.hashCode();
      }
    }
    return hashCode;
  }
}
