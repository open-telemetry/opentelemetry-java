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

package io.opentelemetry.sdk.contrib.trace.aws.resource;

import com.google.common.annotations.VisibleForTesting;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.resources.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

/** Populates the attributes for creating a {@link Resource} that is running in AWS. */
public abstract class AwsResourcePopulator {

  /**
   * Returns a {@link Resource} which is filled with attributes describing the current AWS
   * environment, e.g., metadata for the instance if the app is running on EC2.
   */
  public static Resource createResource() {
    return createResource(new Ec2ResourcePopulator());
  }

  @VisibleForTesting
  static Resource createResource(AwsResourcePopulator... populators) {
    Map<String, AttributeValue> resourceAttributes = new LinkedHashMap<>();

    for (AwsResourcePopulator populator : populators) {
      populator.populate(resourceAttributes);
    }

    return Resource.create(resourceAttributes);
  }

  /** Populates a {@link Map} of attributes for constructing a {@link Resource}. */
  public abstract void populate(Map<String, AttributeValue> resourceAttributes);
}
