/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.resource;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.ResourceProvider;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;

class LambdaResourceTest {
  @Test
  void shouldNotCreateResourceForNotLambda() {
    // given
    ResourceProvider resource = new LambdaResource(emptyMap());

    // when
    Attributes attributes = resource.create().getAttributes();

    // then
    assertTrue(attributes.isEmpty());
  }

  @Test
  void shouldAddNonEmptyAttributes() {
    // given
    ResourceProvider resource =
        new LambdaResource(singletonMap("AWS_LAMBDA_FUNCTION_NAME", "my-function"));

    // when
    Attributes attributes = resource.create().getAttributes();

    // then
    assertThat(attributes)
        .isEqualTo(
            Attributes.of(
                SemanticAttributes.CLOUD_PROVIDER,
                "aws",
                SemanticAttributes.FAAS_NAME,
                "my-function"));
  }

  @Test
  void shouldAddAllAttributes() {
    // given
    Map<String, String> envVars = new HashMap<>();
    envVars.put("AWS_REGION", "us-east-1");
    envVars.put("AWS_LAMBDA_FUNCTION_NAME", "my-function");
    envVars.put("AWS_LAMBDA_FUNCTION_VERSION", "1.2.3");

    ResourceProvider resource = new LambdaResource(envVars);

    // when
    Attributes attributes = resource.create().getAttributes();

    // then
    assertThat(attributes)
        .isEqualTo(
            Attributes.of(
                SemanticAttributes.CLOUD_PROVIDER,
                "aws",
                SemanticAttributes.CLOUD_REGION,
                "us-east-1",
                SemanticAttributes.FAAS_NAME,
                "my-function",
                SemanticAttributes.FAAS_VERSION,
                "1.2.3"));
  }

  @Test
  void inServiceLoader() {
    // No practical way to test the attributes themselves so at least check the service loader picks
    // it up.
    assertThat(ServiceLoader.load(ResourceProvider.class))
        .anyMatch(LambdaResource.class::isInstance);
  }
}
