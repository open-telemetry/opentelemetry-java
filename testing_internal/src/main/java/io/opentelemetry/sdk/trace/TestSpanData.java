package io.opentelemetry.sdk.trace;

import io.opentelemetry.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceState;
import java.util.Collections;

public final class TestSpanData {

  // Intentionally calling AutoValue constructor for testing.
  @SuppressWarnings("AutoValueSubclassLeaked")
  public static SpanData.Builder newBuilder() {
    return new AutoValue_SpanDataImpl.Builder()
        .setParentSpanId(SpanId.getInvalid())
        .setInstrumentationLibraryInfo(InstrumentationLibraryInfo.getEmpty())
        .setLinks(Collections.<SpanData.Link>emptyList())
        .setTotalRecordedLinks(0)
        .setAttributes(Attributes.empty())
        .setEvents(Collections.<SpanData.Event>emptyList())
        .setTotalRecordedEvents(0)
        .setResource(Resource.getEmpty())
        .setTraceState(TraceState.getDefault())
        .setTraceFlags(TraceFlags.getDefault())
        .setHasRemoteParent(false)
        .setTotalAttributeCount(0);
  }

  private TestSpanData() {}
}
