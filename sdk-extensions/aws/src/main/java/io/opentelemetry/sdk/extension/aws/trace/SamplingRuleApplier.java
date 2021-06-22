/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.trace;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

final class SamplingRuleApplier {

  private final Sampler fixedRateSampler;

  private final Map<String, Matcher> attributeMatchers;
  private final Matcher urlPathMatcher;
  private final Matcher serviceNameMatcher;
  private final Matcher httpMethodMatcher;
  private final Matcher hostMatcher;
  private final Matcher serviceTypeMatcher;
  private final Matcher resourceArnMatcher;

  SamplingRuleApplier(GetSamplingRulesResponse.SamplingRule rule) {
    fixedRateSampler = Sampler.parentBased(Sampler.traceIdRatioBased(rule.getFixedRate()));

    if (rule.getAttributes().isEmpty()) {
      attributeMatchers = Collections.emptyMap();
    } else {
      attributeMatchers =
          rule.getAttributes().entrySet().stream()
              .collect(Collectors.toMap(Map.Entry::getKey, e -> toMatcher(e.getValue())));
    }

    urlPathMatcher = toMatcher(rule.getUrlPath());
    serviceNameMatcher = toMatcher(rule.getServiceName());
    httpMethodMatcher = toMatcher(rule.getHttpMethod());
    hostMatcher = toMatcher(rule.getHost());
    serviceTypeMatcher = toMatcher(rule.getServiceType());
    resourceArnMatcher = toMatcher(rule.getResourceArn());
  }

  boolean matches(String name, Attributes attributes, Resource resource) {
    int matchedAttributes = 0;
    String httpTarget = null;
    String httpMethod = null;
    String host = null;

    for (Map.Entry<AttributeKey<?>, Object> entry : attributes.asMap().entrySet()) {
      if (entry.getKey().equals(SemanticAttributes.HTTP_TARGET)) {
        httpTarget = (String) entry.getValue();
      } else if (entry.getKey().equals(SemanticAttributes.HTTP_METHOD)) {
        httpMethod = (String) entry.getValue();
      } else if (entry.getKey().equals(SemanticAttributes.HTTP_HOST)) {
        host = (String) entry.getValue();
      }

      Matcher matcher = attributeMatchers.get(entry.getKey().getKey());
      if (matcher == null) {
        continue;
      }
      if (matcher.matches(entry.getValue().toString())) {
        matchedAttributes++;
      } else {
        return false;
      }
    }
    // All attributes in the matched attributes must have been present in the span to be a match.
    if (matchedAttributes != attributeMatchers.size()) {
      return false;
    }

    return urlPathMatcher.matches(httpTarget)
        && serviceNameMatcher.matches(name)
        && httpMethodMatcher.matches(httpMethod)
        && hostMatcher.matches(host)
        && serviceTypeMatcher.matches(getServiceType(resource))
        && resourceArnMatcher.matches(getArn(attributes, resource));
  }

  SamplingResult shouldSample(
      Context parentContext,
      String traceId,
      String name,
      SpanKind spanKind,
      Attributes attributes,
      List<LinkData> parentLinks) {
    return fixedRateSampler.shouldSample(
        parentContext, traceId, name, spanKind, attributes, parentLinks);
  }

  @Nullable
  private static String getArn(Attributes attributes, Resource resource) {
    String arn = resource.getAttributes().get(ResourceAttributes.AWS_ECS_CONTAINER_ARN);
    if (arn != null) {
      return arn;
    }
    String cloudPlatform = resource.getAttributes().get(ResourceAttributes.CLOUD_PLATFORM);
    if (cloudPlatform == null
        || !cloudPlatform.equals(ResourceAttributes.CloudPlatformValues.AWS_LAMBDA)) {
      return null;
    }
    arn = resource.getAttributes().get(ResourceAttributes.FAAS_ID);
    if (arn != null) {
      return arn;
    }
    return attributes.get(ResourceAttributes.FAAS_ID);
  }

  @Nullable
  private static String getServiceType(Resource resource) {
    String cloudPlatform = resource.getAttributes().get(ResourceAttributes.CLOUD_PLATFORM);
    if (cloudPlatform == null) {
      return null;
    }
    switch (cloudPlatform) {
      case ResourceAttributes.CloudPlatformValues.AWS_EC2:
        return "AWS::EC2::Instance";
      case ResourceAttributes.CloudPlatformValues.AWS_ECS:
        return "AWS::ECS::Container";
      case ResourceAttributes.CloudPlatformValues.AWS_EKS:
        return "AWS::EKS::Container";
      case ResourceAttributes.CloudPlatformValues.AWS_ELASTIC_BEANSTALK:
        return "AWS::ElasticBeanstalk::Environment";
      case ResourceAttributes.CloudPlatformValues.AWS_LAMBDA:
        return "AWS::Lambda::Function";
      default:
        return null;
    }
  }

  private static Matcher toMatcher(String globPattern) {
    if (globPattern.equals("*")) {
      return TrueMatcher.INSTANCE;
    }

    for (int i = 0; i < globPattern.length(); i++) {
      char c = globPattern.charAt(i);
      if (c == '*' || c == '?') {
        return new PatternMatcher(toRegexPattern(globPattern));
      }
    }

    return new StringMatcher(globPattern);
  }

  private static Pattern toRegexPattern(String globPattern) {
    int tokenStart = -1;
    StringBuilder patternBuilder = new StringBuilder();
    for (int i = 0; i < globPattern.length(); i++) {
      char c = globPattern.charAt(i);
      if (c == '*' || c == '?') {
        if (tokenStart != -1) {
          patternBuilder.append(Pattern.quote(globPattern.substring(tokenStart, i)));
          tokenStart = -1;
        }
        if (c == '*') {
          patternBuilder.append(".*");
        } else {
          // c == '?'
          patternBuilder.append(".");
        }
      } else {
        if (tokenStart == -1) {
          tokenStart = i;
        }
      }
    }
    if (tokenStart != -1) {
      patternBuilder.append(Pattern.quote(globPattern.substring(tokenStart)));
    }
    return Pattern.compile(patternBuilder.toString());
  }

  private interface Matcher {
    boolean matches(@Nullable String s);
  }

  private enum TrueMatcher implements Matcher {
    INSTANCE;

    @Override
    public boolean matches(@Nullable String s) {
      return true;
    }

    @Override
    public String toString() {
      return "TrueMatcher";
    }
  }

  private static class StringMatcher implements Matcher {

    private final String target;

    StringMatcher(String target) {
      this.target = target;
    }

    @Override
    public boolean matches(@Nullable String s) {
      if (s == null) {
        return false;
      }
      return target.equalsIgnoreCase(s);
    }

    @Override
    public String toString() {
      return target;
    }
  }

  private static class PatternMatcher implements Matcher {
    private final Pattern pattern;

    PatternMatcher(Pattern pattern) {
      this.pattern = pattern;
    }

    @Override
    public boolean matches(@Nullable String s) {
      if (s == null) {
        return false;
      }
      return pattern.matcher(s).matches();
    }

    @Override
    public String toString() {
      return pattern.toString();
    }
  }
}
