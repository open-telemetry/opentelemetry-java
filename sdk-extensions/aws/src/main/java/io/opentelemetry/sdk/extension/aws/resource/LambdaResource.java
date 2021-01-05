/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.resource;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.resources.ResourceProvider;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import java.util.Map;
import java.util.stream.Stream;

/** A {@link ResourceProvider} which provides information about the AWS Lambda function. */
public final class LambdaResource extends ResourceProvider {
  private final Map<String, String> environmentVariables;

  public LambdaResource() {
    this(System.getenv());
  }

  // Visible for testing
  LambdaResource(Map<String, String> environmentVariables) {
    this.environmentVariables = environmentVariables;
  }

  @Override
  protected Attributes getAttributes() {
    String region = environmentVariables.getOrDefault("AWS_REGION", "");
    String functionName = environmentVariables.getOrDefault("AWS_LAMBDA_FUNCTION_NAME", "");
    String functionVersion = environmentVariables.getOrDefault("AWS_LAMBDA_FUNCTION_VERSION", "");

    if (!isLambda(functionName, functionVersion)) {
      return Attributes.empty();
    }

    AttributesBuilder builder =
        Attributes.builder()
            .put(SemanticAttributes.CLOUD_PROVIDER, SemanticAttributes.CloudProviderValues.AWS);

    if (!region.isEmpty()) {
      builder.put(SemanticAttributes.CLOUD_REGION, region);
    }
    if (!functionName.isEmpty()) {
      builder.put(SemanticAttributes.FAAS_NAME, functionName);
    }
    if (!functionVersion.isEmpty()) {
      builder.put(SemanticAttributes.FAAS_VERSION, functionVersion);
    }

    return builder.build();
  }

  private static boolean isLambda(String... envVariables) {
    return Stream.of(envVariables).anyMatch(v -> !v.isEmpty());
  }
}
