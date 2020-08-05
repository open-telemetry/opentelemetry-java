package io.opentelemetry.sdk.trace.data;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.testing.EqualsTester;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.sdk.trace.data.test.TestSpanData;
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
      AttributeValue userAgent = delegate.getAttributes()
          .get(SemanticAttributes.HTTP_USER_AGENT.key());
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
  void equals() {
    SpanData spanData = createBasicSpanBuilder().build();
    SpanData noopWrapper = new NoOpDelegatingSpanData(spanData);
    SpanData spanDataWithClientType = new SpanDataWithClientType(spanData);
    new EqualsTester()
        // DRAFT: Currently fails because AutoValue can't compare with other interface subclasses :(
        .addEqualityGroup(spanData, noopWrapper)
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