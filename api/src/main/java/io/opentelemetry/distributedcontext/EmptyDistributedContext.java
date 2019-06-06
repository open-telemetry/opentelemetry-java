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

package io.opentelemetry.distributedcontext;

import java.util.Collections;
import java.util.Iterator;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * An immutable implementation of the {@link DistributedContext} that does not contain any entries.
 */
@Immutable
public class EmptyDistributedContext implements DistributedContext {
  private static final Iterator<Entry> EMPTY_ITERATOR = Collections.<Entry>emptyList().iterator();

  /** Returns the single instance of the {@link EmptyDistributedContext} class. */
  public static final DistributedContext INSTANCE = new EmptyDistributedContext();

  @Override
  public Iterator<Entry> getIterator() {
    return EMPTY_ITERATOR;
  }

  @Nullable
  @Override
  public EntryValue getEntryValue(EntryKey entryKey) {
    return null;
  }

  private EmptyDistributedContext() {}
}
