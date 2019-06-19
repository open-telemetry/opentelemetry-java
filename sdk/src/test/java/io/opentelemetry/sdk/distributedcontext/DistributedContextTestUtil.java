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

import static com.google.common.collect.Lists.newArrayList;

import io.opentelemetry.distributedcontext.DistributedContext;
import io.opentelemetry.distributedcontext.Entry;
import java.util.Arrays;
import java.util.List;

class DistributedContextTestUtil {

  static DistributedContextSdk listToDistributedContext(Entry... entries) {
    return listToDistributedContext(Arrays.asList(entries));
  }

  static DistributedContextSdk listToDistributedContext(List<Entry> entries) {
    DistributedContextSdk.Builder builder = DistributedContextSdk.builder();
    for (Entry entry : entries) {
      builder.put(entry.getKey(), entry.getValue(), entry.getEntryMetadata());
    }
    return builder.build();
  }

  static List<Entry> distContextToList(DistributedContext distContext) {
    return newArrayList(distContext.getIterator());
  }

  private DistributedContextTestUtil() {}
}
