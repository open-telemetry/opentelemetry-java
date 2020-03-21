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
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceConstants;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/** Provides for lookup and population of {@link Resource} labels when running on AWS EC2. */
public class Ec2Resource {

  /** OpenTelemetry semantic convention identifier for AWS cloud. */
  static final AttributeValue CLOUD_PROVIDER_AWS = AttributeValue.stringAttributeValue("aws");

  /**
   * Returns a resource with all host and cloud labels populated with the information obtained from
   * the EC2 metadata endpoint.
   *
   * @return the resource
   */
  public static Resource getResource() {
    return getResourceFromInfoAndHost(
        EC2MetadataUtils.getInstanceInfo(), EC2MetadataUtils.getLocalHostName());
  }

  // This can be tested now with a fake info and host.
  static Resource getResourceFromInfoAndHost(
      @Nullable InstanceInfo info, @Nullable String hostname) {
    Map<String, AttributeValue> labels = new HashMap<>();
    labels.put(ResourceConstants.CLOUD_PROVIDER, CLOUD_PROVIDER_AWS);
    if (info != null) {
      labels.put(
          ResourceConstants.CLOUD_ACCOUNT,
          AttributeValue.stringAttributeValue(info.getAccountId()));
      labels.put(
          ResourceConstants.CLOUD_REGION, AttributeValue.stringAttributeValue(info.getRegion()));
      labels.put(
          ResourceConstants.CLOUD_ZONE,
          AttributeValue.stringAttributeValue(info.getAvailabilityZone()));
      labels.put(
          ResourceConstants.HOST_ID, AttributeValue.stringAttributeValue(info.getInstanceId()));
      labels.put(
          ResourceConstants.HOST_NAME, AttributeValue.stringAttributeValue(info.getPrivateIp()));
      labels.put(
          ResourceConstants.HOST_TYPE, AttributeValue.stringAttributeValue(info.getInstanceType()));
      labels.put(
          ResourceConstants.HOST_IMAGE_ID, AttributeValue.stringAttributeValue(info.getImageId()));
    }
    if (!isNullOrEmpty(hostname)) {
      labels.put(ResourceConstants.HOST_HOSTNAME, AttributeValue.stringAttributeValue(hostname));
    }
    return Resource.create(labels);
  }

  private Ec2Resource() {}
}
