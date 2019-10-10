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

package io.opentelemetry.sdk.distributedcontext;

import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.distributedcontext.DistributedContext;
import io.opentelemetry.distributedcontext.Entry;
import io.opentelemetry.distributedcontext.EntryKey;
import io.opentelemetry.distributedcontext.EntryValue;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** W3C Trace Context implementation of the HttpTextFormat for a DistributedContext. */
class DistributedContextHttpTextFormat implements HttpTextFormat<DistributedContext> {
  static final String TRACEPARENT = "traceparent";
  static final String TRACESTATE = "tracestate";
  private static final List<String> FIELDS =
      Collections.unmodifiableList(Arrays.asList(TRACEPARENT, TRACESTATE));

  private static final DistributedContextHttpTextFormat INSTANCE =
      new DistributedContextHttpTextFormat();

  public static DistributedContextHttpTextFormat getInstance() {
    return INSTANCE;
  }

  @Override
  public List<String> fields() {
    return FIELDS;
  }

  @Override
  public <C> void inject(DistributedContext value, C carrier, Setter<C> setter) {
    for (String field : fields()) {
      final EntryValue entryValue = value.getEntryValue(EntryKey.create(field));
      if (entryValue != null) {
        setter.put(carrier, field, entryValue.asString());
      }
    }
  }

  @Override
  public <C> DistributedContext extract(C carrier, Getter<C> getter) {
    final DistributedContextSdk.Builder builder = new DistributedContextSdk.Builder();
    for (String field : fields()) {
      final String value = getter.get(carrier, field);
      if (value != null) {
        builder.put(
            EntryKey.create(field), EntryValue.create(value), Entry.METADATA_UNLIMITED_PROPAGATION);
      }
    }
    return builder.build();
  }
}
