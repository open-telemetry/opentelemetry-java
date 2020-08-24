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
import io.opentelemetry.sdk.resources.ResourceConstants;
import io.opentelemetry.sdk.resources.ResourceProvider;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link ResourceProvider} which provides information about the current ECS container if running
 * on AWS ECS.
 */
public class EcsResource extends ResourceProvider {

  private static final Logger logger = Logger.getLogger(EcsResource.class.getName());

  private static final String ECS_METADATA_KEY_V4 = "ECS_CONTAINER_METADATA_URI_V4";
  private static final String ECS_METADATA_KEY_V3 = "ECS_CONTAINER_METADATA_URI";

  private final Map<String, String> sysEnv;
  private final DockerHelper dockerHelper;

  /**
   * Returns a {@link Ec2Resource} which attempts to compute information about this ECS container if
   * available.
   */
  public EcsResource() {
    this(System.getenv(), new DockerHelper());
  }

  @VisibleForTesting
  EcsResource(Map<String, String> sysEnv, DockerHelper dockerHelper) {
    this.sysEnv = sysEnv;
    this.dockerHelper = dockerHelper;
  }

  @Override
  public Attributes getAttributes() {
    if (!isOnEcs()) {
      return Attributes.Factory.empty();
    }

    Attributes.Builder attrBuilders = Attributes.Factory.newBuilder();
    try {
      String hostName = InetAddress.getLocalHost().getHostName();
      attrBuilders.setAttribute(ResourceConstants.CONTAINER_NAME, hostName);
    } catch (UnknownHostException e) {
      logger.log(Level.WARNING, "Could not get docker container name from hostname.", e);
    }

    String containerId = dockerHelper.getContainerId();
    if (!Strings.isNullOrEmpty(containerId)) {
      attrBuilders.setAttribute(ResourceConstants.CONTAINER_ID, containerId);
    }

    return attrBuilders.build();
  }

  private boolean isOnEcs() {
    return (!Strings.isNullOrEmpty(sysEnv.get(ECS_METADATA_KEY_V3))
        || !Strings.isNullOrEmpty(sysEnv.get(ECS_METADATA_KEY_V4)));
  }
}
