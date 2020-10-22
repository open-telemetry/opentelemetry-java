/*
 * Copyright 2020, OpenTelemetry Authors
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
package io.opentelemetry.sdk.extension.jfr;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import static java.util.Objects.nonNull;

import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.trace.SpanContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Span processor to create new JFR events for the Span as they are started, and
 * commit on end.
 *
 * <p>
 * NOTE: JfrSpanProcessor must be running synchronously to ensure that duration
 * is correctly captured.
 */
public class JfrSpanProcessor implements SpanProcessor {

    private final Map<SpanContext, SpanEvent> spanEvents = new ConcurrentHashMap<>();

    @Override
    public void onStart(ReadWriteSpan span, Context parentContext) {
        if (span.getSpanContext().isValid()) {
            SpanEvent event = new SpanEvent(span.toSpanData());
            event.begin();
            spanEvents.put(span.getSpanContext(), event);
        }
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan rs) {
        SpanEvent event = spanEvents.remove(rs.getSpanContext());
        if (nonNull(event) && event.shouldCommit()) {
            event.commit();
        }
    }

    @Override
    public boolean isEndRequired() {
        return true;
    }

    @Override
    public CompletableResultCode shutdown() {
        spanEvents.forEach((id, event) -> event.commit());
        spanEvents.clear();
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode forceFlush() {
        return CompletableResultCode.ofSuccess();
    }
}
