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

package io.opentelemetry.contrib.http.core;

import com.google.common.base.Splitter;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.distributedcontext.DistributedContext;
import io.opentelemetry.distributedcontext.Entry;
import io.opentelemetry.distributedcontext.EntryKey;
import io.opentelemetry.distributedcontext.EntryMetadata;
import io.opentelemetry.distributedcontext.EntryMetadata.EntryTtl;
import io.opentelemetry.distributedcontext.EntryValue;
import java.util.Arrays;
import java.util.List;

/** Used to make implementations work until SDK implementation is available. */
class TemporaryDistributedContextTextFormat implements HttpTextFormat<DistributedContext> {

  static final String TRACESTATE = "tracestate";
  static final EntryMetadata DEFAULT_METADATA =
      EntryMetadata.create(EntryTtl.UNLIMITED_PROPAGATION);

  @Override
  public List<String> fields() {
    return Arrays.asList(TRACESTATE);
  }

  @Override
  public <C> void inject(DistributedContext value, C carrier, Setter<C> setter) {
    StringBuilder builder = new StringBuilder();
    for (Entry entry : value.getEntries()) {
      if (!entry.getEntryMetadata().getEntryTtl().equals(EntryTtl.NO_PROPAGATION)) {
        if (builder.length() > 0) {
          builder.append(",");
        }
        builder.append(entry.getKey()).append("=").append(entry.getValue());
      }
    }
    setter.put(carrier, TRACESTATE, builder.toString());
  }

  @Override
  public <C> DistributedContext extract(C carrier, Getter<C> getter) {
    String tracestate = getter.get(carrier, TRACESTATE);
    DistributedContext.Builder builder =
        OpenTelemetry.getDistributedContextManager().contextBuilder();
    builder.setNoParent();
    if (tracestate != null && !tracestate.isEmpty()) {
      for (String kvp : Splitter.on(',').split(tracestate)) {
        builder.put(
            EntryKey.create(kvp.substring(0, kvp.indexOf('='))),
            EntryValue.create(kvp.substring(kvp.indexOf('=') + 1)),
            DEFAULT_METADATA);
      }
    }
    return builder.build();
  }
}
