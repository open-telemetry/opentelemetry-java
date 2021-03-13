/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.resource;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A factory for a {@link Resource} which provides information about the current ECS container if
 * running on AWS ECS.
 */
public final class EcsResource {
  private static final Logger logger = Logger.getLogger(EcsResource.class.getName());

  private static final String ECS_METADATA_KEY_V4 = "ECS_CONTAINER_METADATA_URI_V4";
  private static final String ECS_METADATA_KEY_V3 = "ECS_CONTAINER_METADATA_URI";

  private static final Resource INSTANCE = buildResource();

  /**
   * Returns a factory for a {@link Resource} which provides information about the current ECS
   * container if running on AWS ECS.
   */
  public static Resource get() {
    return INSTANCE;
  }

  private static Resource buildResource() {
    return buildResource(System.getenv(), new DockerHelper());
  }

  // Visible for testing
  static Resource buildResource(Map<String, String> sysEnv, DockerHelper dockerHelper) {
    if (!isOnEcs(sysEnv)) {
      return Resource.empty();
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

    return Resource.create(attrBuilders.build());
  }

  private static boolean isOnEcs(Map<String, String> sysEnv) {
    return !sysEnv.getOrDefault(ECS_METADATA_KEY_V3, "").isEmpty()
        || !sysEnv.getOrDefault(ECS_METADATA_KEY_V4, "").isEmpty();
  }

  private EcsResource() {}
}
