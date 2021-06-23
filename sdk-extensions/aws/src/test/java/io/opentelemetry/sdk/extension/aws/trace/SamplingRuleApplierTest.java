/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.trace;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SamplingRuleApplierTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Nested
  @SuppressWarnings("ClassCanBeStatic")
  class ExactMatch {

    private final SamplingRuleApplier applier =
        new SamplingRuleApplier(readSamplingRule("/sampling-rule-exactmatch.json"));

    private final Resource resource =
        Resource.builder()
            .put(ResourceAttributes.CLOUD_PLATFORM, ResourceAttributes.CloudPlatformValues.AWS_EKS)
            .put(
                ResourceAttributes.AWS_ECS_CONTAINER_ARN,
                "arn:aws:xray:us-east-1:595986152929:my-service")
            .build();

    private final Attributes attributes =
        Attributes.builder()
            .put(SemanticAttributes.HTTP_METHOD, "GET")
            .put(SemanticAttributes.HTTP_HOST, "opentelemetry.io")
            .put(SemanticAttributes.HTTP_TARGET, "/instrument-me")
            .put(AttributeKey.stringKey("animal"), "cat")
            .put(AttributeKey.longKey("speed"), 10)
            .build();

    // FixedRate set to 1.0 in rule
    @Test
    void fixedRateAlwaysSample() {
      assertThat(
              applier.shouldSample(
                  Context.current(),
                  TraceId.fromLongs(1, 2),
                  "span",
                  SpanKind.CLIENT,
                  Attributes.empty(),
                  Collections.emptyList()))
          .isEqualTo(SamplingResult.create(SamplingDecision.RECORD_AND_SAMPLE));
    }

    @Test
    void matches() {
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isTrue();
    }

    @Test
    void nameNotMatch() {
      assertThat(applier.matches("test-service-foo-baz", attributes, resource)).isFalse();
    }

    @Test
    void nullNotMatch() {
      assertThat(applier.matches(null, attributes, resource)).isFalse();
    }

    @Test
    void methodNotMatch() {
      Attributes attributes =
          this.attributes.toBuilder().put(SemanticAttributes.HTTP_METHOD, "POST").build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
    }

    @Test
    void hostNotMatch() {
      // Replacing dot with character makes sure we're not accidentally treating dot as regex
      // wildcard.
      Attributes attributes =
          this.attributes.toBuilder().put(SemanticAttributes.HTTP_HOST, "opentelemetryfio").build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
    }

    @Test
    void pathNotMatch() {
      Attributes attributes =
          this.attributes.toBuilder()
              .put(SemanticAttributes.HTTP_TARGET, "/instrument-you")
              .build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
    }

    @Test
    void attributeNotMatch() {
      Attributes attributes =
          this.attributes.toBuilder().put(AttributeKey.stringKey("animal"), "dog").build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
    }

    @Test
    void attributeMissing() {
      Attributes attributes = removeAttribute(this.attributes, AttributeKey.stringKey("animal"));
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
    }

    @Test
    void serviceTypeNotMatch() {
      Resource resource =
          this.resource.toBuilder()
              .put(
                  ResourceAttributes.CLOUD_PLATFORM, ResourceAttributes.CloudPlatformValues.AWS_EC2)
              .build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
      resource =
          Resource.create(
              removeAttribute(this.resource.getAttributes(), ResourceAttributes.CLOUD_PLATFORM));
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
    }

    @Test
    void arnNotMatch() {
      Resource resource =
          this.resource.toBuilder()
              .put(
                  ResourceAttributes.AWS_ECS_CONTAINER_ARN,
                  "arn:aws:xray:us-east-1:595986152929:my-service2")
              .build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
    }
  }

  @Nested
  @SuppressWarnings("ClassCanBeStatic")
  class WildcardMatch {

    private final SamplingRuleApplier applier =
        new SamplingRuleApplier(readSamplingRule("/sampling-rule-wildcards.json"));

    private final Resource resource =
        Resource.builder()
            .put(ResourceAttributes.CLOUD_PLATFORM, ResourceAttributes.CloudPlatformValues.AWS_EKS)
            .put(
                ResourceAttributes.AWS_ECS_CONTAINER_ARN,
                "arn:aws:xray:us-east-1:595986152929:my-service")
            .build();

    private final Attributes attributes =
        Attributes.builder()
            .put(SemanticAttributes.HTTP_METHOD, "GET")
            .put(SemanticAttributes.HTTP_HOST, "opentelemetry.io")
            .put(SemanticAttributes.HTTP_TARGET, "/instrument-me?foo=bar&cat=meow")
            .put(AttributeKey.stringKey("animal"), "cat")
            .put(AttributeKey.longKey("speed"), 10)
            .build();

    // FixedRate set to 0.0 in rule
    @Test
    void fixedRateNeverSample() {
      assertThat(
              applier.shouldSample(
                  Context.current(),
                  TraceId.fromLongs(1, 2),
                  "span",
                  SpanKind.CLIENT,
                  Attributes.empty(),
                  Collections.emptyList()))
          .isEqualTo(SamplingResult.create(SamplingDecision.DROP));
    }

    @Test
    void nameMatches() {
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isTrue();
      assertThat(applier.matches("test-service-foo-baz", attributes, resource)).isTrue();
      assertThat(applier.matches("test-service-foo-", attributes, resource)).isTrue();
    }

    @Test
    void nameNotMatch() {
      assertThat(applier.matches("test-service-foo", attributes, resource)).isFalse();
      assertThat(applier.matches("prod-service-foo-bar", attributes, resource)).isFalse();
      assertThat(applier.matches(null, attributes, resource)).isFalse();
    }

    @Test
    void methodMatches() {
      Attributes attributes =
          this.attributes.toBuilder().put(SemanticAttributes.HTTP_METHOD, "BADGETGOOD").build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isTrue();
      attributes =
          this.attributes.toBuilder().put(SemanticAttributes.HTTP_METHOD, "BADGET").build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isTrue();
      attributes =
          this.attributes.toBuilder().put(SemanticAttributes.HTTP_METHOD, "GETGET").build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isTrue();
    }

    @Test
    void methodNotMatch() {
      Attributes attributes =
          this.attributes.toBuilder().put(SemanticAttributes.HTTP_METHOD, "POST").build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
      attributes = removeAttribute(this.attributes, SemanticAttributes.HTTP_METHOD);
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
    }

    @Test
    void hostMatches() {
      Attributes attributes =
          this.attributes.toBuilder()
              .put(SemanticAttributes.HTTP_HOST, "alpha.opentelemetry.io")
              .build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isTrue();
      attributes =
          this.attributes.toBuilder()
              .put(SemanticAttributes.HTTP_HOST, "opfdnqtelemetry.io")
              .build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isTrue();
      attributes =
          this.attributes.toBuilder().put(SemanticAttributes.HTTP_HOST, "opentglemetry.io").build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isTrue();
      attributes =
          this.attributes.toBuilder().put(SemanticAttributes.HTTP_HOST, "opentglemry.io").build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isTrue();
      attributes =
          this.attributes.toBuilder().put(SemanticAttributes.HTTP_HOST, "opentglemrz.io").build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isTrue();
    }

    @Test
    void hostNotMatch() {
      Attributes attributes =
          this.attributes.toBuilder().put(SemanticAttributes.HTTP_HOST, "opentelemetryfio").build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
      attributes =
          this.attributes.toBuilder()
              .put(SemanticAttributes.HTTP_HOST, "opentgalemetry.io")
              .build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
      attributes =
          this.attributes.toBuilder()
              .put(SemanticAttributes.HTTP_HOST, "alpha.oentelemetry.io")
              .build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
      attributes = removeAttribute(this.attributes, SemanticAttributes.HTTP_HOST);
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
    }

    @Test
    void pathMatches() {
      Attributes attributes =
          this.attributes.toBuilder()
              .put(SemanticAttributes.HTTP_TARGET, "/instrument-me?foo=bar&cat=")
              .build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isTrue();
      // Deceptive question mark, it's actually a wildcard :-)
      attributes =
          this.attributes.toBuilder()
              .put(SemanticAttributes.HTTP_TARGET, "/instrument-meafoo=bar&cat=")
              .build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isTrue();
    }

    @Test
    void pathNotMatch() {
      Attributes attributes =
          this.attributes.toBuilder()
              .put(SemanticAttributes.HTTP_TARGET, "/instrument-mea?foo=bar&cat=")
              .build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
      attributes =
          this.attributes.toBuilder()
              .put(SemanticAttributes.HTTP_TARGET, "foo/instrument-meafoo=bar&cat=")
              .build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
      attributes = removeAttribute(this.attributes, SemanticAttributes.HTTP_TARGET);
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
    }

    @Test
    void attributeMatches() {
      Attributes attributes =
          this.attributes.toBuilder().put(AttributeKey.stringKey("animal"), "catman").build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isTrue();
      attributes = this.attributes.toBuilder().put(AttributeKey.longKey("speed"), 20).build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isTrue();
    }

    @Test
    void attributeNotMatch() {
      Attributes attributes =
          this.attributes.toBuilder().put(AttributeKey.stringKey("animal"), "dog").build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
      attributes =
          this.attributes.toBuilder().put(AttributeKey.stringKey("animal"), "mancat").build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
      attributes = this.attributes.toBuilder().put(AttributeKey.longKey("speed"), 21).build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
    }

    @Test
    void attributeMissing() {
      Attributes attributes = removeAttribute(this.attributes, AttributeKey.stringKey("animal"));
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
    }

    @Test
    void serviceTypeMatches() {
      Resource resource =
          this.resource.toBuilder()
              .put(
                  ResourceAttributes.CLOUD_PLATFORM, ResourceAttributes.CloudPlatformValues.AWS_EC2)
              .build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isTrue();
      resource =
          Resource.create(
              removeAttribute(this.resource.getAttributes(), ResourceAttributes.CLOUD_PLATFORM));
      // null matches for pattern '*'
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isTrue();
    }

    @Test
    void arnMatches() {
      Resource resource =
          this.resource.toBuilder()
              .put(
                  ResourceAttributes.AWS_ECS_CONTAINER_ARN,
                  "arn:aws:opentelemetry:us-east-3:52929:my-service")
              .build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isTrue();
    }

    @Test
    void arnNotMatch() {
      Resource resource =
          this.resource.toBuilder()
              .put(
                  ResourceAttributes.AWS_ECS_CONTAINER_ARN,
                  "arn:aws:xray:us-east-1:595986152929:my-service2")
              .build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
      resource =
          this.resource.toBuilder()
              .put(
                  ResourceAttributes.AWS_ECS_CONTAINER_ARN,
                  "frn:aws:xray:us-east-1:595986152929:my-service")
              .build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
      resource =
          Resource.create(
              removeAttribute(
                  this.resource.getAttributes(), ResourceAttributes.AWS_ECS_CONTAINER_ARN));
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
    }
  }

  @Nested
  @SuppressWarnings("ClassCanBeStatic")
  class AwsLambdaTest {

    private final SamplingRuleApplier applier =
        new SamplingRuleApplier(readSamplingRule("/sampling-rule-awslambda.json"));

    private final Resource resource =
        Resource.builder()
            .put(
                ResourceAttributes.CLOUD_PLATFORM,
                ResourceAttributes.CloudPlatformValues.AWS_LAMBDA)
            .put(ResourceAttributes.FAAS_ID, "arn:aws:xray:us-east-1:595986152929:my-service")
            .build();

    private final Attributes attributes =
        Attributes.builder()
            .put(SemanticAttributes.HTTP_METHOD, "GET")
            .put(SemanticAttributes.HTTP_HOST, "opentelemetry.io")
            .put(SemanticAttributes.HTTP_TARGET, "/instrument-me")
            .put(AttributeKey.stringKey("animal"), "cat")
            .put(AttributeKey.longKey("speed"), 10)
            .build();

    @Test
    void resourceFaasIdMatches() {
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isTrue();
    }

    @Test
    void spanFaasIdMatches() {
      Resource resource =
          Resource.create(
              removeAttribute(this.resource.getAttributes(), ResourceAttributes.FAAS_ID));
      Attributes attributes =
          this.attributes.toBuilder()
              .put(ResourceAttributes.FAAS_ID, "arn:aws:xray:us-east-1:595986152929:my-service")
              .build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isTrue();
    }

    @Test
    void notLambdaNotMatches() {
      Resource resource =
          this.resource.toBuilder()
              .put(
                  ResourceAttributes.CLOUD_PLATFORM,
                  ResourceAttributes.CloudPlatformValues.GCP_CLOUD_FUNCTIONS)
              .build();
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
      resource =
          Resource.create(
              removeAttribute(this.resource.getAttributes(), ResourceAttributes.CLOUD_PLATFORM));
      assertThat(applier.matches("test-service-foo-bar", attributes, resource)).isFalse();
    }
  }

  private static GetSamplingRulesResponse.SamplingRule readSamplingRule(String resourcePath) {
    try {
      return OBJECT_MAPPER.readValue(
          SamplingRuleApplierTest.class.getResource(resourcePath),
          GetSamplingRulesResponse.SamplingRule.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static Attributes removeAttribute(Attributes attributes, AttributeKey<?> removedKey) {
    AttributesBuilder builder = Attributes.builder();
    // TODO(anuraaga): Replace with AttributeBuilder.remove
    attributes.forEach(
        (key, value) -> {
          if (!key.equals(removedKey)) {
            builder.put((AttributeKey) key, value);
          }
        });
    return builder.build();
  }
}
