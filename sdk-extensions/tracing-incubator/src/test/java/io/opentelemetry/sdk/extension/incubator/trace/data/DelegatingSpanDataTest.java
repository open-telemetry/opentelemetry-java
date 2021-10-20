/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.data;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.testing.EqualsTester;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.semconv.v1.trace.attributes.SemanticAttributes;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation") // Tests deprecated class
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
      AttributesBuilder newAttributes = Attributes.builder();
      newAttributes.putAll(delegate.getAttributes());
      newAttributes.put("client_type", clientType);
      attributes = newAttributes.build();
    }

    @Override
    public Attributes getAttributes() {
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
    assertThat(noopWrapper)
        .usingRecursiveComparison(
            RecursiveComparisonConfiguration.builder().withIgnoredFields("delegate").build())
        .isEqualTo(spanData);
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
        .setName("spanName")
        .setStartEpochNanos(100)
        .setEndEpochNanos(200)
        .setKind(SpanKind.SERVER)
        .setStatus(StatusData.ok())
        .setTotalRecordedEvents(0)
        .setTotalRecordedLinks(0);
  }
}
