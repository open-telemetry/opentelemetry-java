/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.resource;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.common.annotations.VisibleForTesting;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.attributes.SemanticAttributes;
import io.opentelemetry.sdk.resources.ResourceProvider;
import java.util.Map;

/** A {@link ResourceProvider} which provides information about the AWS Lambda function. */
public final class LambdaResource extends ResourceProvider {
  private final Map<String, String> environmentVariables;

  public LambdaResource() {
    this(System.getenv());
  }

  @VisibleForTesting
  LambdaResource(Map<String, String> environmentVariables) {
    this.environmentVariables = environmentVariables;
  }

  @Override
  protected Attributes getAttributes() {
    if (!isLambda()) {
      return Attributes.empty();
    }
    String region = environmentVariables.get("AWS_REGION");
    String functionName = environmentVariables.get("AWS_LAMBDA_FUNCTION_NAME");
    String functionVersion = environmentVariables.get("AWS_LAMBDA_FUNCTION_VERSION");

    AttributesBuilder builder =
        Attributes.builder()
            .put(SemanticAttributes.CLOUD_PROVIDER, SemanticAttributes.CloudProviderValues.AWS)
            .put(SemanticAttributes.FAAS_NAME, functionName);

    if (!isNullOrEmpty(region)) {
      builder.put(SemanticAttributes.CLOUD_REGION, region);
    }
    if (!isNullOrEmpty(functionVersion)) {
      builder.put(SemanticAttributes.FAAS_VERSION, functionVersion);
    }

    return builder.build();
  }

  private boolean isLambda() {
    return !isNullOrEmpty(environmentVariables.get("AWS_LAMBDA_FUNCTION_NAME"));
  }
}
