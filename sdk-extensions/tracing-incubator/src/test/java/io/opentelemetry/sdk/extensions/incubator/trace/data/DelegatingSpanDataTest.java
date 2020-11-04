/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.incubator.trace.data;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.testing.EqualsTester;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.ReadableAttributes;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.attributes.SemanticAttributes;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Status;
import org.junit.jupiter.api.Test;

class DelegatingSpanDataTest {

  private static final AttributeKey<String> CLIENT_TYPE_KEY = stringKey("client_type");

  private static final class NoOpDelegatingSpanData extends DelegatingSpanData {
    private NoOpDelegatingSpanData(SpanData delegate) {
      super(delegate);
    }
  }

  private static final class SpanDataWithClientType extends DelegatingSpanData {

    private final Attributes attributes;

    private SpanDataWithClientType(SpanData delegate) {
      super(delegate);
      final String clientType;
      String userAgent = delegate.getAttributes().get(SemanticAttributes.HTTP_USER_AGENT);
      if (userAgent != null) {
        clientType = parseUserAgent(userAgent);
      } else {
        clientType = "unknown";
      }
      Attributes.Builder newAttributes = Attributes.builder();
      delegate.getAttributes().forEach(newAttributes::put);
      newAttributes.put("client_type", clientType);
      attributes = newAttributes.build();
    }

    @Override
    public ReadableAttributes getAttributes() {
      return attributes;
    }

    private static String parseUserAgent(String userAgent) {
      if (userAgent.startsWith("Mozilla/")) {
        return "browser";
      } else if (userAgent.startsWith("Phone/")) {
        return "phone";
      }
      return "unknown";
    }
  }

  @Test
  void delegates() {
    SpanData spanData = createBasicSpanBuilder().build();
    SpanData noopWrapper = new NoOpDelegatingSpanData(spanData);
    // Test should always verify delegation is working even when methods are added since it calls
    // each method individually.
    assertThat(noopWrapper).isEqualToIgnoringGivenFields(spanData, "delegate");
  }

  @Test
  void overrideDelegate() {
    SpanData spanData = createBasicSpanBuilder().build();
    SpanData spanDataWithClientType = new SpanDataWithClientType(spanData);
    assertThat(spanDataWithClientType.getAttributes().get(CLIENT_TYPE_KEY)).isEqualTo("unknown");
  }

  @Test
  void equals() {
    SpanData spanData = createBasicSpanBuilder().build();
    SpanData noopWrapper = new NoOpDelegatingSpanData(spanData);
    SpanData spanDataWithClientType = new SpanDataWithClientType(spanData);

    assertThat(noopWrapper).isEqualTo(spanData);
    // TODO(anuraaga): Bug - spanData.equals(noopWrapper) should be equal but AutoValue does not
    // implement equals for interfaces properly. We can't add it as a separate group either since
    // noopWrapper.equals(spanData) does work properly.
    assertThat(spanData).isNotEqualTo(noopWrapper);

    new EqualsTester()
        .addEqualityGroup(noopWrapper)
        .addEqualityGroup(spanDataWithClientType)
        .testEquals();
    assertThat(spanDataWithClientType.getAttributes().get(CLIENT_TYPE_KEY)).isEqualTo("unknown");
  }

  private static TestSpanData.Builder createBasicSpanBuilder() {
    return TestSpanData.builder()
        .setHasEnded(true)
        .setSpanId(SpanId.getInvalid())
        .setTraceId(TraceId.getInvalid())
        .setName("spanName")
        .setStartEpochNanos(100)
        .setEndEpochNanos(200)
        .setKind(Kind.SERVER)
        .setStatus(Status.ok())
        .setHasRemoteParent(false)
        .setTotalRecordedEvents(0)
        .setTotalRecordedLinks(0);
  }
}
