/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.resource;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;

import com.google.common.io.Resources;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EcsResourceTest {
  private static final String ECS_METADATA_KEY_V4 = "ECS_CONTAINER_METADATA_URI_V4";
  private static final String ECS_METADATA_KEY_V3 = "ECS_CONTAINER_METADATA_URI";

  @Mock private SimpleHttpClient mockHttpClient;

  @Test
  void testCreateAttributesV3() throws IOException {
    Map<String, String> mockSysEnv = new HashMap<>();
    mockSysEnv.put(ECS_METADATA_KEY_V3, "ecs_metadata_v3_uri");
    when(mockHttpClient.fetchString("GET", "ecs_metadata_v3_uri", Collections.emptyMap(), null))
        .thenReturn(readResourceJson("ecs-container-metadata-v3.json"));
    when(mockHttpClient.fetchString(
            "GET", "ecs_metadata_v3_uri/task", Collections.emptyMap(), null))
        .thenReturn(readResourceJson("ecs-task-metadata-v3.json"));

    Resource resource = EcsResource.buildResource(mockSysEnv, mockHttpClient);
    Attributes attributes = resource.getAttributes();

    assertThat(resource.getSchemaUrl()).isEqualTo(ResourceAttributes.SCHEMA_URL);
    assertThat(attributes)
        .containsOnly(
            entry(ResourceAttributes.CLOUD_PROVIDER, "aws"),
            entry(ResourceAttributes.CLOUD_PLATFORM, "aws_ecs"),
            entry(ResourceAttributes.CONTAINER_NAME, "ecs-nginx-5-nginx-curl-ccccb9f49db0dfe0d901"),
            entry(
                ResourceAttributes.CONTAINER_ID,
                "43481a6ce4842eec8fe72fc28500c6b52edcc0917f105b83379f88cac1ff3946"),
            entry(ResourceAttributes.CONTAINER_IMAGE_NAME, "nrdlngr/nginx-curl"),
            entry(ResourceAttributes.CONTAINER_IMAGE_TAG, "latest"),
            entry(
                AttributeKey.stringKey("aws.ecs.container.image.id"),
                "sha256:2e00ae64383cfc865ba0a2ba37f61b50a120d2d9378559dcd458dc0de47bc165"),
            entry(
                ResourceAttributes.AWS_ECS_TASK_ARN,
                "arn:aws:ecs:us-east-2:012345678910:task/9781c248-0edd-4cdb-9a93-f63cb662a5d3"),
            entry(ResourceAttributes.AWS_ECS_TASK_FAMILY, "nginx"),
            entry(ResourceAttributes.AWS_ECS_TASK_REVISION, "5"));
  }

  @Test
  void testCreateAttributesV4() throws IOException {
    Map<String, String> mockSysEnv = new HashMap<>();
    mockSysEnv.put(ECS_METADATA_KEY_V4, "ecs_metadata_v4_uri");
    when(mockHttpClient.fetchString("GET", "ecs_metadata_v4_uri", Collections.emptyMap(), null))
        .thenReturn(readResourceJson("ecs-container-metadata-v4.json"));
    when(mockHttpClient.fetchString(
            "GET", "ecs_metadata_v4_uri/task", Collections.emptyMap(), null))
        .thenReturn(readResourceJson("ecs-task-metadata-v4.json"));

    Resource resource = EcsResource.buildResource(mockSysEnv, mockHttpClient);
    Attributes attributes = resource.getAttributes();

    assertThat(resource.getSchemaUrl()).isEqualTo(ResourceAttributes.SCHEMA_URL);
    assertThat(attributes)
        .containsOnly(
            entry(ResourceAttributes.CLOUD_PROVIDER, "aws"),
            entry(ResourceAttributes.CLOUD_PLATFORM, "aws_ecs"),
            entry(ResourceAttributes.CONTAINER_NAME, "ecs-curltest-26-curl-cca48e8dcadd97805600"),
            entry(
                ResourceAttributes.CONTAINER_ID,
                "ea32192c8553fbff06c9340478a2ff089b2bb5646fb718b4ee206641c9086d66"),
            entry(
                ResourceAttributes.CONTAINER_IMAGE_NAME,
                "111122223333.dkr.ecr.us-west-2.amazonaws.com/curltest"),
            entry(ResourceAttributes.CONTAINER_IMAGE_TAG, "latest"),
            entry(
                AttributeKey.stringKey("aws.ecs.container.image.id"),
                "sha256:d691691e9652791a60114e67b365688d20d19940dde7c4736ea30e660d8d3553"),
            entry(
                ResourceAttributes.AWS_ECS_CONTAINER_ARN,
                "arn:aws:ecs:us-west-2:111122223333:container/0206b271-b33f-47ab-86c6-a0ba208a70a9"),
            entry(
                ResourceAttributes.AWS_LOG_GROUP_NAMES, Collections.singletonList("/ecs/metadata")),
            entry(
                ResourceAttributes.AWS_LOG_GROUP_ARNS,
                Collections.singletonList(
                    "arn:aws:logs:us-west-2:111122223333:log-group:/ecs/metadata:*")),
            entry(
                ResourceAttributes.AWS_LOG_STREAM_NAMES,
                Collections.singletonList("ecs/curl/8f03e41243824aea923aca126495f665")),
            entry(
                ResourceAttributes.AWS_LOG_STREAM_ARNS,
                Collections.singletonList(
                    "arn:aws:logs:us-west-2:111122223333:log-group:/ecs/metadata:log-stream:ecs/curl/8f03e41243824aea923aca126495f665")),
            entry(
                ResourceAttributes.AWS_ECS_TASK_ARN,
                "arn:aws:ecs:us-west-2:111122223333:task/default/158d1c8083dd49d6b527399fd6414f5c"),
            entry(ResourceAttributes.AWS_ECS_LAUNCHTYPE, "ec2"),
            entry(ResourceAttributes.AWS_ECS_TASK_FAMILY, "curltest"),
            entry(ResourceAttributes.AWS_ECS_TASK_REVISION, "26"));
  }

  @Test
  void testNotOnEcs() {
    Map<String, String> mockSysEnv = new HashMap<>();
    mockSysEnv.put(ECS_METADATA_KEY_V3, "");
    mockSysEnv.put(ECS_METADATA_KEY_V4, "");
    Attributes attributes = EcsResource.buildResource(mockSysEnv, mockHttpClient).getAttributes();
    assertThat(attributes).isEmpty();
  }

  @Test
  void inServiceLoader() {
    // No practical way to test the attributes themselves so at least check the service loader picks
    // it up.
    assertThat(ServiceLoader.load(ResourceProvider.class))
        .anyMatch(EcsResourceProvider.class::isInstance);
  }

  String readResourceJson(String resourceName) throws IOException {
    return Resources.toString(Resources.getResource(resourceName), StandardCharsets.UTF_8);
  }
}
