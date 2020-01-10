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

import static com.google.common.base.Strings.isNullOrEmpty;

import com.amazonaws.util.EC2MetadataUtils;
import com.amazonaws.util.EC2MetadataUtils.InstanceInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceConstants;
import java.util.HashMap;
import java.util.Map;

/** Provides for lookup and population of {@link Resource} labels when running on AWS EC2. */
public class Ec2Resource {

  /** OpenTelemetry semantic convention identifier for AWS cloud. */
  public static final String CLOUD_PROVIDER_AWS = "aws";

  /**
   * Returns a resource with all host and cloud labels populated with the information obtained from
   * the EC2 metadata endpoint.
   *
   * @return the resource
   */
  public static Resource getResource() {
    Map<String, String> labels = new HashMap<>();
    labels.put(ResourceConstants.CLOUD_PROVIDER, CLOUD_PROVIDER_AWS);
    addEc2InstanceData(labels);
    return Resource.create(labels);
  }

  private static void addEc2InstanceData(Map<String, String> labels) {
    InstanceInfo info = EC2MetadataUtils.getInstanceInfo();
    if (info != null) {
      labels.put(ResourceConstants.CLOUD_ACCOUNT, info.getAccountId());
      labels.put(ResourceConstants.CLOUD_REGION, info.getRegion());
      labels.put(ResourceConstants.CLOUD_ZONE, info.getAvailabilityZone());
      labels.put(ResourceConstants.HOST_ID, info.getInstanceId());
      labels.put(ResourceConstants.HOST_NAME, info.getPrivateIp());
      labels.put(ResourceConstants.HOST_TYPE, info.getInstanceType());
    }
    String hostname = EC2MetadataUtils.getLocalHostName();
    if (!isNullOrEmpty(hostname)) {
      labels.put(ResourceConstants.HOST_HOSTNAME, hostname);
    }
  }

  private Ec2Resource() {}
}
