/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.json;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** Converter from SDK {@link SpanData} to OTLP JSON. */
public final class JsonSpanAdapter {
    // In practice, there is often only one thread that calls this code in the BatchSpanProcessor so
    // reusing buffers for the thread is almost free. Even with multiple threads, it should still be
    // worth it and is common practice in serialization libraries such as Jackson.
    private static final ThreadLocal<ThreadLocalCache> THREAD_LOCAL_CACHE = new ThreadLocal<>();

    private static final JSONObject STATUS_OK = new JSONObject();
    private static final JSONObject STATUS_ERROR = new JSONObject();
    private static final JSONObject STATUS_UNSET = new JSONObject();

    static {
        STATUS_OK.put("code", "STATUS_CODE_OK");
        STATUS_OK.put("deprecated_code", "DEPRECATED_STATUS_CODE_OK");

        STATUS_ERROR.put("code", "STATUS_CODE_ERROR");
        STATUS_ERROR.put("deprecated_code", "DEPRECATED_STATUS_CODE_UNKNOWN_ERROR");

        STATUS_UNSET.put("code", "STATUS_CODE_UNSET");
        STATUS_UNSET.put("deprecated_code", "DEPRECATED_STATUS_CODE_OK");
    }

    /** Converts the provided {@link SpanData} to JSONArray. */
    public static JSONArray toJsonResourceSpans(Collection<SpanData> spanDataList) {
        Map<Resource, Map<InstrumentationLibraryInfo, JSONArray>> resourceAndLibraryMap =
                groupByResourceAndLibrary(spanDataList);
        JSONArray resourceSpans = new JSONArray(resourceAndLibraryMap.size());
        resourceAndLibraryMap.forEach(
                (resource, librarySpans) -> {
                    JSONObject resourceSpan = new JSONObject();
                    if (resource.getSchemaUrl() != null) {
                        resourceSpan.put("schema_url", resource.getSchemaUrl());
                    }
                    JSONArray instrumentationLibrarySpan = new JSONArray();
                    librarySpans.forEach(
                            (library, spans) -> {
                                instrumentationLibrarySpan.add(
                                        buildInstrumentationLibrarySpan(library, spans));
                            });
                    resourceSpan.put("instrumentation_library_spans", instrumentationLibrarySpan);
                    resourceSpan.put("resource", JsonResourceAdapter.toProtoResource(resource));
                    resourceSpans.add(resourceSpan);
                });
        return resourceSpans;
    }

    private static Map<Resource, Map<InstrumentationLibraryInfo, JSONArray>>
    groupByResourceAndLibrary(Collection<SpanData> spanDataList) {
        Map<Resource, Map<InstrumentationLibraryInfo, JSONArray>> result = new HashMap<>();
        ThreadLocalCache threadLocalCache = getThreadLocalCache();
        for (SpanData spanData : spanDataList) {
            Map<InstrumentationLibraryInfo, JSONArray> libraryInfoListMap =
                    result.computeIfAbsent(spanData.getResource(), unused -> new HashMap<>());
            JSONArray spanList =
                    libraryInfoListMap.computeIfAbsent(
                            spanData.getInstrumentationLibraryInfo(), unused -> new JSONArray());
            spanList.add(toProtoSpan(spanData, threadLocalCache));
        }
        threadLocalCache.idBytesCache.clear();
        return result;
    }

    static JSONObject toProtoSpan(SpanData spanData, ThreadLocalCache threadLocalCache) {
        JSONObject builder = threadLocalCache.spanBuilder;
        // We reuse the builder instance to create multiple spans to reduce allocation of intermediary
        // storage. It means we MUST clear here or we'd keep on building on the same object.
        builder.clear();
        builder.put("trace_id", spanData.getSpanContext().getTraceId());
        builder.put("span_id", spanData.getSpanContext().getSpanId());
        if (spanData.getParentSpanContext().isValid()) {
            builder.put("parent_span_id", spanData.getParentSpanContext().getSpanId());
        }
        builder.put("name", spanData.getName());
        builder.put("kind", toProtoSpanKind(spanData.getKind()));
        builder.put("start_time_unix_nano", spanData.getStartEpochNanos());
        builder.put("end_time_unix_nano", spanData.getEndEpochNanos());
        JSONArray attributes = new JSONArray();
        spanData
                .getAttributes()
                .forEach((key, value) -> attributes.add(JsonCommonAdapter.toJsonAttribute(key, value)));
        builder.put("attributes", attributes);
        builder.put("dropped_attributes_count",
                spanData.getTotalAttributeCount() - spanData.getAttributes().size());
        JSONArray events = new JSONArray();
        for (EventData event : spanData.getEvents()) {
            events.add(toProtoSpanEvent(event, threadLocalCache));
        }
        builder.put("events", events);
        builder.put("dropped_events_count",
                spanData.getTotalRecordedEvents() - spanData.getEvents().size());
        JSONArray links = new JSONArray();
        for (LinkData link : spanData.getLinks()) {
            links.add(toProtoSpanLink(link, threadLocalCache));
        }
        builder.put("links", links);
        builder.put("dropped_links_count",
                spanData.getTotalRecordedLinks() - spanData.getLinks().size());
        builder.put("status", toStatusProto(spanData.getStatus()));
        return builder;
    }

    static JSONObject toProtoSpanEvent(EventData event, ThreadLocalCache threadLocalCache) {
        JSONObject builder = threadLocalCache.spanEventBuilder;
        // We reuse the builder instance to create multiple spans to reduce allocation of intermediary
        // storage. It means we MUST clear here or we'd keep on building on the same object.
        builder.clear();
        builder.put("name",event.getName());
        builder.put("time_unix_nano", event.getEpochNanos());
        JSONArray attributes = new JSONArray();
        event
                .getAttributes()
                .forEach((key, value) -> attributes.add(JsonCommonAdapter.toJsonAttribute(key, value)));
        builder.put("attributes", attributes);
        builder.put("dropped_attributes_count",
                event.getTotalAttributeCount() - event.getAttributes().size());
        return builder;
    }

    static JSONObject toProtoSpanLink(LinkData link, ThreadLocalCache threadLocalCache) {
        JSONObject builder = threadLocalCache.spanLinkBuilder;
        // We reuse the builder instance to create multiple spans to reduce allocation of intermediary
        // storage. It means we MUST clear here or we'd keep on building on the same object.
        builder.clear();
        builder.put("trace_id", link.getSpanContext().getTraceId());
        builder.put("span_id", link.getSpanContext().getSpanId());
        // TODO: Set TraceState;
        JSONArray attributesArray = new JSONArray();
        Attributes attributes = link.getAttributes();
        attributes.forEach(
                (key, value) -> attributesArray.add(JsonCommonAdapter.toJsonAttribute(key, value)));
        builder.put("attributes", attributesArray);
        builder.put("dropped_attributes_count", link.getTotalAttributeCount() - attributes.size());
        return builder;
    }

    static JSONObject toStatusProto(StatusData status) {
        final JSONObject withoutDescription;
        switch (status.getStatusCode()) {
            case OK:
                withoutDescription = STATUS_OK;
                break;
            case ERROR:
                withoutDescription = STATUS_ERROR;
                break;
            case UNSET:
            default:
                withoutDescription = STATUS_UNSET;
                break;
        }
        if (status.getDescription().isEmpty()) {
            return withoutDescription;
        }
        withoutDescription.put("message", status.getDescription());
        return withoutDescription;
    }
    static String toProtoSpanKind(SpanKind kind) {
        switch (kind) {
            case INTERNAL:
                return "SPAN_KIND_INTERNAL";
            case SERVER:
                return "SPAN_KIND_SERVER";
            case CLIENT:
                return "SPAN_KIND_CLIENT";
            case PRODUCER:
                return "SPAN_KIND_PRODUCER";
            case CONSUMER:
                return "SPAN_KIND_CONSUMER";
        }
        return "UNRECOGNIZED";
    }

    private static JSONObject buildInstrumentationLibrarySpan(
            InstrumentationLibraryInfo library, JSONArray spans) {
        JSONObject spansBuilder = new JSONObject();
        spansBuilder.put("instrumentation_library", JsonCommonAdapter.toProtoInstrumentationLibrary(library));
        spansBuilder.put("spans", spans);
        if (library.getSchemaUrl() != null) {
            spansBuilder.put("schema_url", library.getSchemaUrl());
        }
        return spansBuilder;
    }

    private static ThreadLocalCache getThreadLocalCache() {
        ThreadLocalCache result = THREAD_LOCAL_CACHE.get();
        if (result == null) {
            result = new ThreadLocalCache();
            THREAD_LOCAL_CACHE.set(result);
        }
        return result;
    }

    static final class ThreadLocalCache {
        final Map<String, ByteString> idBytesCache = new HashMap<>();
        final JSONObject spanBuilder = new JSONObject();
        final JSONObject spanEventBuilder = new JSONObject();
        final JSONObject spanLinkBuilder = new JSONObject();
    }

    private JsonSpanAdapter() {}
}
