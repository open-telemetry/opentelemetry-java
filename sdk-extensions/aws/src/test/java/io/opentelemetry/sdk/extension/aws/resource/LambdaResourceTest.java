/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.resource;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;

class LambdaResourceTest {
  @Test
  void shouldNotCreateResourceForNotLambda() {
    Attributes attributes = LambdaResource.buildResource(emptyMap()).getAttributes();
    assertThat(attributes.isEmpty()).isTrue();
  }

  @Test
  void shouldAddNonEmptyAttributes() {
    Resource resource =
        LambdaResource.buildResource(singletonMap("AWS_LAMBDA_FUNCTION_NAME", "my-function"));
    Attributes attributes = resource.getAttributes();

    assertThat(resource.getSchemaUrl()).isEqualTo(ResourceAttributes.SCHEMA_URL);
    assertThat(attributes)
        .isEqualTo(
            Attributes.of(
                ResourceAttributes.CLOUD_PROVIDER,
                "aws",
                ResourceAttributes.FAAS_NAME,
                "my-function"));
  }

  @Test
  void shouldAddAllAttributes() {
    Map<String, String> envVars = new HashMap<>();
    envVars.put("AWS_REGION", "us-east-1");
    envVars.put("AWS_LAMBDA_FUNCTION_NAME", "my-function");
    envVars.put("AWS_LAMBDA_FUNCTION_VERSION", "1.2.3");

    Resource resource = LambdaResource.buildResource(envVars);
    Attributes attributes = resource.getAttributes();

    assertThat(resource.getSchemaUrl()).isEqualTo(ResourceAttributes.SCHEMA_URL);
    assertThat(attributes)
        .isEqualTo(
            Attributes.of(
                ResourceAttributes.CLOUD_PROVIDER,
                "aws",
                ResourceAttributes.CLOUD_REGION,
                "us-east-1",
                ResourceAttributes.FAAS_NAME,
                "my-function",
                ResourceAttributes.FAAS_VERSION,
                "1.2.3"));
  }

  @Test
  void inServiceLoader() {
    // No practical way to test the attributes themselves so at least check the service loader picks
    // it up.
    assertThat(ServiceLoader.load(ResourceProvider.class))
        .anyMatch(LambdaResourceProvider.class::isInstance);
  }
}
