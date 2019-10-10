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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.distributedcontext.DistributedContext;
import io.opentelemetry.distributedcontext.Entry;
import io.opentelemetry.distributedcontext.EntryKey;
import io.opentelemetry.distributedcontext.EntryValue;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.junit.Test;

public class DistributedContextHttpTextFormatTest {

  @Test
  public void inject() {
    DistributedContextHttpTextFormat httpTextFormat =
        DistributedContextHttpTextFormat.getInstance();

    Map<String, String> dataCarrier = new HashMap<>();
    DistributedContext distributedContext =
        new DistributedContextSdk.Builder()
            .put(
                EntryKey.create("foo"),
                EntryValue.create("bar"),
                Entry.METADATA_UNLIMITED_PROPAGATION)
            .put(
                EntryKey.create(DistributedContextHttpTextFormat.TRACEPARENT),
                EntryValue.create("traceParentData"),
                Entry.METADATA_UNLIMITED_PROPAGATION)
            .put(
                EntryKey.create(DistributedContextHttpTextFormat.TRACESTATE),
                EntryValue.create("traceStateData"),
                Entry.METADATA_UNLIMITED_PROPAGATION)
            .build();

    httpTextFormat.inject(
        distributedContext,
        dataCarrier,
        new HttpTextFormat.Setter<Map<String, String>>() {
          @Override
          public void put(Map<String, String> carrier, String key, String value) {
            carrier.put(key, value);
          }
        });

    assertEquals(2, dataCarrier.size());
    assertEquals("traceParentData", dataCarrier.get(DistributedContextHttpTextFormat.TRACEPARENT));
    assertEquals("traceStateData", dataCarrier.get(DistributedContextHttpTextFormat.TRACESTATE));
  }

  @Test
  public void inject_nullValues() {
    DistributedContextHttpTextFormat httpTextFormat =
        DistributedContextHttpTextFormat.getInstance();

    Map<String, String> dataCarrier = new HashMap<>();
    DistributedContext distributedContext =
        new DistributedContextSdk.Builder()
            .put(
                EntryKey.create("foo"),
                EntryValue.create("bar"),
                Entry.METADATA_UNLIMITED_PROPAGATION)
            .build();

    httpTextFormat.inject(
        distributedContext,
        dataCarrier,
        new HttpTextFormat.Setter<Map<String, String>>() {
          @Override
          public void put(Map<String, String> carrier, String key, String value) {
            carrier.put(key, value);
          }
        });

    assertTrue(dataCarrier.isEmpty());
  }

  @Test
  public void extract() throws Exception {
    DistributedContextHttpTextFormat httpTextFormat =
        DistributedContextHttpTextFormat.getInstance();
    Map<String, String> dataCarrier = new HashMap<>();
    dataCarrier.put(DistributedContextHttpTextFormat.TRACESTATE, "traceStateData");
    dataCarrier.put(DistributedContextHttpTextFormat.TRACEPARENT, "traceParentData");

    DistributedContext context =
        httpTextFormat.extract(
            dataCarrier,
            new HttpTextFormat.Getter<Map<String, String>>() {
              @Nullable
              @Override
              public String get(Map<String, String> carrier, String key) {
                return carrier.get(key);
              }
            });

    assertEquals(2, context.getEntries().size());
    assertEquals(
        EntryValue.create("traceStateData"),
        context.getEntryValue(EntryKey.create(DistributedContextHttpTextFormat.TRACESTATE)));
    assertEquals(
        EntryValue.create("traceParentData"),
        context.getEntryValue(EntryKey.create(DistributedContextHttpTextFormat.TRACEPARENT)));
  }

  @Test
  public void extract_missingValues() throws Exception {
    DistributedContextHttpTextFormat httpTextFormat =
        DistributedContextHttpTextFormat.getInstance();
    Map<String, String> dataCarrier = new HashMap<>();

    DistributedContext context =
        httpTextFormat.extract(
            dataCarrier,
            new HttpTextFormat.Getter<Map<String, String>>() {
              @Nullable
              @Override
              public String get(Map<String, String> carrier, String key) {
                return carrier.get(key);
              }
            });

    assertEquals(0, context.getEntries().size());
  }
}
