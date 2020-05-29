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

package io.opentelemetry.sdk.contrib.trace.aws;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.contrib.trace.aws.resource.AwsResourcePopulator;
import io.opentelemetry.sdk.resources.Resource;

/**
 * Provides for lookup and population of {@link Resource} labels when running on AWS EC2.
 *
 * @deprecated Use {@link AwsResourcePopulator}.
 */
@Deprecated
public class Ec2Resource {

  /** OpenTelemetry semantic convention identifier for AWS cloud. */
  static final AttributeValue CLOUD_PROVIDER_AWS = AttributeValue.stringAttributeValue("aws");

  /**
   * Returns a resource with all host and cloud labels populated with the information obtained from
   * the EC2 metadata endpoint.
   *
   * @return the resource
   * @deprecated Use {@link AwsResourcePopulator#createResource()}.
   */
  @Deprecated
  public static Resource getResource() {
    return AwsResourcePopulator.createResource();
  }

  private Ec2Resource() {}
}
