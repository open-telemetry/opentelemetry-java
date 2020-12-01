/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.resource;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.resources.ResourceAttributes;
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

  // Visible for testing
  EcsResource(Map<String, String> sysEnv, DockerHelper dockerHelper) {
    this.sysEnv = sysEnv;
    this.dockerHelper = dockerHelper;
  }

  @Override
  public Attributes getAttributes() {
    if (!isOnEcs()) {
      return Attributes.empty();
    }

    AttributesBuilder attrBuilders = Attributes.builder();
    attrBuilders.put(ResourceAttributes.CLOUD_PROVIDER, AwsResourceConstants.cloudProvider());
    try {
      String hostName = InetAddress.getLocalHost().getHostName();
      attrBuilders.put(ResourceAttributes.CONTAINER_NAME, hostName);
    } catch (UnknownHostException e) {
      logger.log(Level.WARNING, "Could not get docker container name from hostname.", e);
    }

    String containerId = dockerHelper.getContainerId();
    if (containerId != null && !containerId.isEmpty()) {
      attrBuilders.put(ResourceAttributes.CONTAINER_ID, containerId);
    }

    return attrBuilders.build();
  }

  private boolean isOnEcs() {
    return !sysEnv.getOrDefault(ECS_METADATA_KEY_V3, "").isEmpty()
        || !sysEnv.getOrDefault(ECS_METADATA_KEY_V4, "").isEmpty();
  }
}
