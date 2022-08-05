/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.resource;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
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

  private static final JsonFactory JSON_FACTORY = new JsonFactory();

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
    attrBuilders.put(ResourceAttributes.CLOUD_PROVIDER, ResourceAttributes.CloudProviderValues.AWS);
    attrBuilders.put(
        ResourceAttributes.CLOUD_PLATFORM, ResourceAttributes.CloudPlatformValues.AWS_ECS);
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

    String metadataUriV4 = sysEnv.getOrDefault(ECS_METADATA_KEY_V4, "");
    if (!metadataUriV4.isEmpty()) {
      try {
        attrBuilders.put(
            ResourceAttributes.AWS_ECS_CONTAINER_ARN,
            getContainerArn(fetchMetadata(metadataUriV4)));
      } catch (Exception e) {
        logger.log(
            Level.WARNING, "Could not get the container ARN from the Metadata V4 endpoint.", e);
      }

      try {
        String taskMetadata = fetchMetadata(metadataUriV4 + "/task");

        try (JsonParser parser = JSON_FACTORY.createParser(taskMetadata)) {
          parser.nextToken();

          if (!parser.isExpectedStartObjectToken()) {
            throw new IOException(
                "Invalid JSON returned by the Metadata v4 '/task' endpoint:" + taskMetadata);
          }

          String taskArn = null;
          String cluster = null;

          while (parser.nextToken() != JsonToken.END_OBJECT) {
            String value = parser.nextTextValue();
            switch (parser.getCurrentName()) {
              case "Cluster":
                /*
                 * This is not guaranteed to be the Cluster ARN, and we may also need the
                 * Task ARN to recreate the cluster ARN.
                 */
                cluster = value;
                break;
              case "Family":
                attrBuilders.put(ResourceAttributes.AWS_ECS_TASK_FAMILY, value);
                break;
              case "LaunchType":
                attrBuilders.put(ResourceAttributes.AWS_ECS_LAUNCHTYPE, value);
                break;
              case "Revision":
                attrBuilders.put(ResourceAttributes.AWS_ECS_TASK_REVISION, value);
                break;
              case "TaskARN":
                taskArn = value;
                attrBuilders.put(ResourceAttributes.AWS_ECS_TASK_ARN, value);
                break;
              default:
                parser.skipChildren();
            }
          }

          if (taskArn == null) {
            throw new IllegalStateException(
                "The 'TaskARN' field was not provided by the Metadata v4 '/task' endpoint:"
                    + taskMetadata);
          }

          if (cluster == null) {
            throw new IllegalStateException(
                "The 'Cluster' field was not provided by the Metadata v4 '/task' endpoint:"
                    + taskMetadata);
          } else if (cluster.startsWith("arn:")) {
            attrBuilders.put(ResourceAttributes.AWS_ECS_CLUSTER_ARN, cluster);
          } else {
            String baseArn = taskArn.substring(0, taskArn.lastIndexOf(":"));
            attrBuilders.put(
                ResourceAttributes.AWS_ECS_CLUSTER_ARN, baseArn + ":cluster/" + cluster);
          }
        }
      } catch (Exception e) {
        logger.log(
            Level.WARNING,
            "Could not extract resource attributes from the Metadata V4 '/task' endpoint.",
            e);
      }
    }

    return Resource.create(attrBuilders.build(), ResourceAttributes.SCHEMA_URL);
  }

  private static boolean isOnEcs(Map<String, String> sysEnv) {
    return !sysEnv.getOrDefault(ECS_METADATA_KEY_V3, "").isEmpty()
        || !sysEnv.getOrDefault(ECS_METADATA_KEY_V4, "").isEmpty();
  }

  private static String getContainerArn(String containerMetadataJson) throws IOException {
    JsonParser parser = JSON_FACTORY.createParser(containerMetadataJson);

    parser.nextToken();
    if (!parser.isExpectedStartObjectToken()) {
      throw new IOException(
          "Invalid JSON returned by the Metadata v4 endpoint:" + containerMetadataJson);
    }

    while (parser.nextToken() != JsonToken.END_OBJECT) {
      String value = parser.nextTextValue();
      switch (parser.getCurrentName()) {
        case "ContainerARN":
          return value;
        default:
          parser.skipChildren();
      }
    }

    throw new IllegalStateException(
        "The JSON returned by the ECS Metadata V4 endpoint does not contain a 'ContainerARN' field.");
  }

  private static String fetchMetadata(String url) {
    return new SimpleHttpClient().fetchString("GET", url.toString(), Collections.emptyMap(), null);
  }

  private EcsResource() {}
}
