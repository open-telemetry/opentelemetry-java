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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.sdk.resources.ResourceAttributes;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

class EcsResource extends AwsResource {

  private static final Logger logger = Logger.getLogger(EcsResource.class.getName());

  private static final String ECS_METADATA_KEY_V4 = "ECS_CONTAINER_METADATA_URI_V4";
  private static final String ECS_METADATA_KEY_V3 = "ECS_CONTAINER_METADATA_URI";

  private final Map<String, String> sysEnv;
  private final DockerHelper dockerHelper;

  EcsResource() {
    this(System.getenv(), new DockerHelper());
  }

  @VisibleForTesting
  EcsResource(Map<String, String> sysEnv, DockerHelper dockerHelper) {
    this.sysEnv = sysEnv;
    this.dockerHelper = dockerHelper;
  }

  @Override
  Attributes createAttributes() {
    if (!isOnEcs()) {
      return Attributes.empty();
    }

    Attributes.Builder attrBuilders = Attributes.newBuilder();
    try {
      String hostName = InetAddress.getLocalHost().getHostName();
      ResourceAttributes.CONTAINER_NAME.set(attrBuilders, hostName);
    } catch (UnknownHostException e) {
      logger.log(Level.WARNING, "Could not get docker container name from hostname.", e);
    }

    String containerId = dockerHelper.getContainerId();
    if (!Strings.isNullOrEmpty(containerId)) {
      ResourceAttributes.CONTAINER_ID.set(attrBuilders, containerId);
    }

    return attrBuilders.build();
  }

  private boolean isOnEcs() {
    return (!Strings.isNullOrEmpty(sysEnv.get(ECS_METADATA_KEY_V3))
        || !Strings.isNullOrEmpty(sysEnv.get(ECS_METADATA_KEY_V4)));
  }
}
