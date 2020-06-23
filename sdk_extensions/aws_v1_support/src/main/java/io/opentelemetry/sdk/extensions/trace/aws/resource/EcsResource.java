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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.resources.ResourceConstants;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

class EcsResource extends AwsResource {

  private static final Logger logger = Logger.getLogger(EcsResource.class.getName());

  private static final String ECS_METADATA_KEY_V4 = "ECS_CONTAINER_METADATA_URI_V4";
  private static final String ECS_METADATA_KEY_V3 = "ECS_CONTAINER_METADATA_URI";
  private static final String CONTAINER_ID = "container.id";

  private final DockerHelper dockerHelper;
  private final String uriV3;
  private final String uriV4;

  EcsResource() {
    this(
        new DockerHelper(), System.getenv(ECS_METADATA_KEY_V3), System.getenv(ECS_METADATA_KEY_V4));
  }

  @VisibleForTesting
  EcsResource(DockerHelper dockerHelper, String uriV3, String uriV4) {
    this.dockerHelper = dockerHelper;
    this.uriV3 = uriV3;
    this.uriV4 = uriV4;
  }

  @Override
  Map<String, AttributeValue> createAttributes() {
    // Check whether we are actually on ECS
    if ((Strings.isNullOrEmpty(uriV3) && Strings.isNullOrEmpty(uriV4))) {
      return ImmutableMap.of();
    }

    ImmutableMap.Builder<String, AttributeValue> resourceAttributes = ImmutableMap.builder();
    try {
      String hostName = InetAddress.getLocalHost().getHostName();
      resourceAttributes.put(ResourceConstants.CONTAINER_NAME, stringAttributeValue(hostName));
    } catch (UnknownHostException e) {
      logger.log(Level.WARNING, "Could not get docker container name from hostname.", e);
    }

    String containerId = dockerHelper.getContainerId();
    if (!Strings.isNullOrEmpty(containerId)) {
      resourceAttributes.put(CONTAINER_ID, stringAttributeValue(containerId));
    }

    return resourceAttributes.build();
  }
}
