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
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/**
 * A factory for a {@link Resource} which provides information about the current ECS container if
 * running on AWS ECS.
 *
 * @deprecated Moved to <a
 *     href="https://github.com/open-telemetry/opentelemetry-java-contrib/tree/main/aws-resources">io.opentelemetry.contrib:opentelemetry-aws-resources</a>.
 */
@Deprecated
public final class EcsResource {
  private static final Logger logger = Logger.getLogger(EcsResource.class.getName());
  private static final JsonFactory JSON_FACTORY = new JsonFactory();
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
    return buildResource(System.getenv(), new SimpleHttpClient());
  }

  // Visible for testing
  static Resource buildResource(Map<String, String> sysEnv, SimpleHttpClient httpClient) {
    // Note: If V4 is set V3 is set as well, so check V4 first.
    String ecsMetadataUrl =
        sysEnv.getOrDefault(ECS_METADATA_KEY_V4, sysEnv.getOrDefault(ECS_METADATA_KEY_V3, ""));
    if (!ecsMetadataUrl.isEmpty()) {
      AttributesBuilder attrBuilders = Attributes.builder();
      fetchMetadata(httpClient, ecsMetadataUrl, attrBuilders);
      // For TaskARN, Family, Revision.
      // May put the same attribute twice but that shouldn't matter.
      fetchMetadata(httpClient, ecsMetadataUrl + "/task", attrBuilders);
      return Resource.create(attrBuilders.build(), ResourceAttributes.SCHEMA_URL);
    }
    // Not running on ECS
    return Resource.empty();
  }

  static void fetchMetadata(
      SimpleHttpClient httpClient, String url, AttributesBuilder attrBuilders) {
    String json = httpClient.fetchString("GET", url, Collections.emptyMap(), null);
    if (json.isEmpty()) {
      return;
    }
    attrBuilders.put(ResourceAttributes.CLOUD_PROVIDER, ResourceAttributes.CloudProviderValues.AWS);
    attrBuilders.put(
        ResourceAttributes.CLOUD_PLATFORM, ResourceAttributes.CloudPlatformValues.AWS_ECS);
    try (JsonParser parser = JSON_FACTORY.createParser(json)) {
      parser.nextToken();
      LogArnBuilder logArnBuilder = new LogArnBuilder();
      parseResponse(parser, attrBuilders, logArnBuilder);

      logArnBuilder
          .getLogGroupArn()
          .ifPresent(
              logGroupArn -> {
                attrBuilders.put(
                    ResourceAttributes.AWS_LOG_GROUP_ARNS, Collections.singletonList(logGroupArn));
              });

      logArnBuilder
          .getLogStreamArn()
          .ifPresent(
              logStreamArn -> {
                attrBuilders.put(
                    ResourceAttributes.AWS_LOG_STREAM_ARNS,
                    Collections.singletonList(logStreamArn));
              });
    } catch (IOException e) {
      logger.log(Level.WARNING, "Can't get ECS metadata", e);
    }
  }

  static void parseResponse(
      JsonParser parser, AttributesBuilder attrBuilders, LogArnBuilder logArnBuilder)
      throws IOException {
    if (!parser.isExpectedStartObjectToken()) {
      logger.log(Level.WARNING, "Couldn't parse ECS metadata, invalid JSON");
      return;
    }

    while (parser.nextToken() != JsonToken.END_OBJECT) {
      String value = parser.nextTextValue();
      switch (parser.getCurrentName()) {
        case "DockerId":
          attrBuilders.put(ResourceAttributes.CONTAINER_ID, value);
          break;
        case "DockerName":
          attrBuilders.put(ResourceAttributes.CONTAINER_NAME, value);
          break;
        case "ContainerARN":
          attrBuilders.put(ResourceAttributes.AWS_ECS_CONTAINER_ARN, value);
          logArnBuilder.setContainerArn(value);
          break;
        case "Image":
          DockerImage parsedImage = DockerImage.parse(value);
          if (parsedImage != null) {
            attrBuilders.put(ResourceAttributes.CONTAINER_IMAGE_NAME, parsedImage.getRepository());
            attrBuilders.put(ResourceAttributes.CONTAINER_IMAGE_TAG, parsedImage.getTag());
          }
          break;
        case "ImageID":
          attrBuilders.put("aws.ecs.container.image.id", value);
          break;
        case "LogOptions":
          // Recursively parse LogOptions
          parseResponse(parser, attrBuilders, logArnBuilder);
          break;
        case "awslogs-group":
          attrBuilders.put(ResourceAttributes.AWS_LOG_GROUP_NAMES, value);
          logArnBuilder.setLogGroupName(value);
          break;
        case "awslogs-stream":
          attrBuilders.put(ResourceAttributes.AWS_LOG_STREAM_NAMES, value);
          logArnBuilder.setLogStreamName(value);
          break;
        case "awslogs-region":
          logArnBuilder.setRegion(value);
          break;
        case "TaskARN":
          attrBuilders.put(ResourceAttributes.AWS_ECS_TASK_ARN, value);
          break;
        case "LaunchType":
          attrBuilders.put(ResourceAttributes.AWS_ECS_LAUNCHTYPE, value.toLowerCase());
          break;
        case "Family":
          attrBuilders.put(ResourceAttributes.AWS_ECS_TASK_FAMILY, value);
          break;
        case "Revision":
          attrBuilders.put(ResourceAttributes.AWS_ECS_TASK_REVISION, value);
          break;
        default:
          parser.skipChildren();
          break;
      }
    }
  }

  private EcsResource() {}

  /**
   * This builder can piece together the ARN of a log group or a log stream from region, account,
   * group name and stream name as the ARN isn't part of the ECS metadata.
   *
   * <p>If we just set AWS_LOG_GROUP_NAMES then the CloudWatch X-Ray traces view displays "An error
   * occurred fetching your data". That's why it's important we set the ARN.
   */
  private static class LogArnBuilder {

    @Nullable String region;
    @Nullable String account;
    @Nullable String logGroupName;
    @Nullable String logStreamName;

    void setRegion(@Nullable String region) {
      this.region = region;
    }

    void setLogGroupName(@Nullable String logGroupName) {
      this.logGroupName = logGroupName;
    }

    void setLogStreamName(@Nullable String logStreamName) {
      this.logStreamName = logStreamName;
    }

    void setContainerArn(@Nullable String containerArn) {
      if (containerArn != null) {
        account = containerArn.split(":")[4];
      }
    }

    Optional<String> getLogGroupArn() {
      if (region == null || account == null || logGroupName == null) {
        return Optional.empty();
      }

      return Optional.of("arn:aws:logs:" + region + ":" + account + ":log-group:" + logGroupName);
    }

    Optional<String> getLogStreamArn() {
      if (region == null || account == null || logGroupName == null || logStreamName == null) {
        return Optional.empty();
      }

      return Optional.of(
          "arn:aws:logs:"
              + region
              + ":"
              + account
              + ":log-group:"
              + logGroupName
              + ":log-stream:"
              + logStreamName);
    }
  }

  /** This can parse a Docker image name into its parts: repository, tag and sha256. */
  private static class DockerImage {

    private static final Pattern imagePattern =
        Pattern.compile(
            "^(?<repository>([^/\\s]+/)?([^:\\s]+))(:(?<tag>[^@\\s]+))?(@sha256:(?<sha256>\\d+))?$");

    final String repository;
    final String tag;

    private DockerImage(String repository, String tag) {
      this.repository = repository;
      this.tag = tag;
    }

    String getRepository() {
      return repository;
    }

    String getTag() {
      return tag;
    }

    @Nullable
    static DockerImage parse(@Nullable String image) {
      if (image == null || image.isEmpty()) {
        return null;
      }
      Matcher matcher = imagePattern.matcher(image);
      if (!matcher.matches()) {
        logger.log(Level.WARNING, "Couldn't parse image '" + image + "'");
        return null;
      }
      String repository = matcher.group("repository");
      String tag = matcher.group("tag");
      if (tag == null || tag.isEmpty()) {
        tag = "latest";
      }
      return new DockerImage(repository, tag);
    }
  }
}
