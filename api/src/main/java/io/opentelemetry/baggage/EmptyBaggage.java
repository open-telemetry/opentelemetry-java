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

package io.opentelemetry.baggage;

import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/** An immutable implementation of the {@link Baggage} that does not contain any entries. */
@Immutable
public class EmptyBaggage implements Baggage {
  /**
   * Returns the single instance of the {@link EmptyBaggage} class.
   *
   * @return the single instance of the {@code EmptyBaggage} class.
   * @since 0.9.0
   */
  public static Baggage getInstance() {
    return INSTANCE;
  }

  private static final Baggage INSTANCE = new EmptyBaggage();

  @Override
  public Collection<Entry> getEntries() {
    return Collections.emptyList();
  }

  @Nullable
  @Override
  public String getEntryValue(String entryKey) {
    return null;
  }

  private EmptyBaggage() {}
}
