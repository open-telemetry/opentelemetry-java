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

package io.opentelemetry.sdk.extensions.trace.aws.resource;

import static io.opentelemetry.common.AttributeValue.stringAttributeValue;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.extensions.trace.aws.resource.utils.DockerUtil;
import io.opentelemetry.sdk.resources.ResourceConstants;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EcsResource extends AwsResource {

  private static final Logger logger = Logger.getLogger(EcsResource.class.getName());

  private static final String ECS_METADATA_KEY_V4 = "ECS_CONTAINER_METADATA_URI_V4";
  private static final String ECS_METADATA_KEY_V3 = "ECS_CONTAINER_METADATA_URI";
  private static final String CONTAINER_ID = "container.id";

  private final DockerUtil dockerUtil;

  public EcsResource(DockerUtil dockerUtil) {
    this.dockerUtil = dockerUtil;
  }

  @Override
  Map<String, AttributeValue> createAttributes() {
    String uriV3 = System.getenv(ECS_METADATA_KEY_V3);
    String uriV4 = System.getenv(ECS_METADATA_KEY_V4);
    if ((uriV3 == null || uriV3.isEmpty()) && (uriV4 == null || uriV4.isEmpty())) {
      return ImmutableMap.of();
    }
    ImmutableMap.Builder<String, AttributeValue> resourceAttributes = ImmutableMap.builder();

    try {
      String hostName = InetAddress.getLocalHost().getHostName();
      resourceAttributes.put(ResourceConstants.CONTAINER_NAME, stringAttributeValue(hostName));
    } catch (UnknownHostException e) {
      logger.log(Level.WARNING, "Could not get docker container name from hostname.", e);
    }

    String containerId = dockerUtil.getContainerId();
    if (containerId != null) {
      resourceAttributes.put(CONTAINER_ID, stringAttributeValue(containerId));
    }

    return resourceAttributes.build();
  }
}
