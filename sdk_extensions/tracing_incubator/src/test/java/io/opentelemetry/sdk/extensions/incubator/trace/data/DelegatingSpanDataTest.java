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

package io.opentelemetry.sdk.extensions.incubator.trace.data;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.testing.EqualsTester;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.sdk.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.attributes.SemanticAttributes;
import org.junit.jupiter.api.Test;

class DelegatingSpanDataTest {

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
      AttributeValue userAgent =
          delegate.getAttributes().get(SemanticAttributes.HTTP_USER_AGENT.key());
      if (userAgent != null) {
        clientType = parseUserAgent(userAgent.getStringValue());
      } else {
        clientType = "unknown";
      }
      Attributes.Builder newAttributes = Attributes.newBuilder();
      delegate.getAttributes().forEach(newAttributes::setAttribute);
      newAttributes.setAttribute("client_type", clientType);
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
    assertThat(spanDataWithClientType.getAttributes().get("client_type").getStringValue())
        .isEqualTo("unknown");
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
    assertThat(spanDataWithClientType.getAttributes().get("client_type").getStringValue())
        .isEqualTo("unknown");
  }

  private static TestSpanData.Builder createBasicSpanBuilder() {
    return TestSpanData.newBuilder()
        .setHasEnded(true)
        .setSpanId(SpanId.getInvalid())
        .setTraceId(TraceId.getInvalid())
        .setName("spanName")
        .setStartEpochNanos(100)
        .setEndEpochNanos(200)
        .setKind(Kind.SERVER)
        .setStatus(Status.OK)
        .setHasRemoteParent(false)
        .setTotalRecordedEvents(0)
        .setTotalRecordedLinks(0);
  }
}
