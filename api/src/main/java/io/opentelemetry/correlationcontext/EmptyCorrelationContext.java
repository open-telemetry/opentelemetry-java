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

package io.opentelemetry.correlationcontext;

import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * An immutable implementation of the {@link CorrelationContext} that does not contain any entries.
 */
@Immutable
public class EmptyCorrelationContext implements CorrelationContext {
  /**
   * Returns the single instance of the {@link EmptyCorrelationContext} class.
   *
   * @return the single instance of the {@code EmptyCorrelationContext} class.
   * @since 0.1.0
   */
  public static CorrelationContext getInstance() {
    return INSTANCE;
  }

  private static final CorrelationContext INSTANCE = new EmptyCorrelationContext();

  @Override
  public Collection<Entry> getEntries() {
    return Collections.emptyList();
  }

  @Nullable
  @Override
  public EntryValue getEntryValue(EntryKey entryKey) {
    return null;
  }

  private EmptyCorrelationContext() {}
}
