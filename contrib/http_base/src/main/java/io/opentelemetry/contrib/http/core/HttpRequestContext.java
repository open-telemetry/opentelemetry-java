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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import io.opentelemetry.distributedcontext.DistributedContext;
import io.opentelemetry.trace.Span;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nullable;

/** This class provides storage per request context on http client and server. */
public class HttpRequestContext {

  @VisibleForTesting static final long INVALID_STARTTIME = -1;

  @VisibleForTesting final long requestStartTime;
  @VisibleForTesting final Span span;
  @VisibleForTesting AtomicLong sentMessageSize = new AtomicLong();
  @VisibleForTesting AtomicLong receiveMessageSize = new AtomicLong();
  @VisibleForTesting AtomicLong sentSeqId = new AtomicLong();
  @VisibleForTesting AtomicLong receviedSeqId = new AtomicLong();
  @VisibleForTesting @Nullable final DistributedContext distContext;

  HttpRequestContext(Span span, @Nullable DistributedContext distContext) {
    checkNotNull(span, "span is required");
    this.span = span;
    this.distContext = distContext;
    requestStartTime = System.nanoTime();
  }
}
