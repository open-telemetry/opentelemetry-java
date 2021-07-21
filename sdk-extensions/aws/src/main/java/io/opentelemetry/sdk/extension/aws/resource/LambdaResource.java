/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.resource;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.util.Map;
import java.util.stream.Stream;

/** A factory for a {@link Resource} which provides information about the AWS Lambda function. */
public final class LambdaResource {

  private static final Resource INSTANCE = buildResource();

  /**
   * Returns a factory for a {@link Resource} which provides information about the AWS Lambda
   * function.
   */
  public static Resource get() {
    return INSTANCE;
  }

  private static Resource buildResource() {
    return buildResource(System.getenv());
  }

  // Visible for testing
  static Resource buildResource(Map<String, String> environmentVariables) {
    String region = environmentVariables.getOrDefault("AWS_REGION", "");
    String functionName = environmentVariables.getOrDefault("AWS_LAMBDA_FUNCTION_NAME", "");
    String functionVersion = environmentVariables.getOrDefault("AWS_LAMBDA_FUNCTION_VERSION", "");

    if (!isLambda(functionName, functionVersion)) {
      return Resource.empty();
    }

    AttributesBuilder builder =
        Attributes.builder()
            .put(ResourceAttributes.CLOUD_PROVIDER, ResourceAttributes.CloudProviderValues.AWS);

    if (!region.isEmpty()) {
      builder.put(ResourceAttributes.CLOUD_REGION, region);
    }
    if (!functionName.isEmpty()) {
      builder.put(ResourceAttributes.FAAS_NAME, functionName);
    }
    if (!functionVersion.isEmpty()) {
      builder.put(ResourceAttributes.FAAS_VERSION, functionVersion);
    }

    return Resource.create(builder.build(), ResourceAttributes.SCHEMA_URL);
  }

  private static boolean isLambda(String... envVariables) {
    return Stream.of(envVariables).anyMatch(v -> !v.isEmpty());
  }

  private LambdaResource() {}
}
