/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.resource;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.testing.junit5.server.mock.MockWebServerExtension;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EcsResourceTest {
  private static final String ECS_METADATA_KEY_V4 = "ECS_CONTAINER_METADATA_URI_V4";
  private static final String ECS_METADATA_KEY_V3 = "ECS_CONTAINER_METADATA_URI";

  private static final String METADATA_V4_RESPONSE =
      "{"
          + "    \"DockerId\": \"ea32192c8553fbff06c9340478a2ff089b2bb5646fb718b4ee206641c9086d66\","
          + "    \"Name\": \"curl\","
          + "    \"DockerName\": \"ecs-curltest-24-curl-cca48e8dcadd97805600\","
          + "    \"Image\": \"111122223333.dkr.ecr.us-west-2.amazonaws.com/curltest:latest\","
          + "    \"ImageID\": \"sha256:d691691e9652791a60114e67b365688d20d19940dde7c4736ea30e660d8d3553\","
          + "    \"Labels\": {"
          + "        \"com.amazonaws.ecs.cluster\": \"default\","
          + "        \"com.amazonaws.ecs.container-name\": \"curl\","
          + "        \"com.amazonaws.ecs.task-arn\": \"arn:aws:ecs:us-west-2:111122223333:task/default/8f03e41243824aea923aca126495f665\","
          + "        \"com.amazonaws.ecs.task-definition-family\": \"curltest\","
          + "        \"com.amazonaws.ecs.task-definition-version\": \"24\""
          + "    },"
          + "    \"DesiredStatus\": \"RUNNING\","
          + "    \"KnownStatus\": \"RUNNING\","
          + "    \"Limits\": {"
          + "        \"CPU\": 10,"
          + "        \"Memory\": 128"
          + "    },"
          + "    \"CreatedAt\": \"2020-10-02T00:15:07.620912337Z\","
          + "    \"StartedAt\": \"2020-10-02T00:15:08.062559351Z\","
          + "    \"Type\": \"NORMAL\","
          + "    \"LogDriver\": \"awslogs\","
          + "    \"LogOptions\": {"
          + "        \"awslogs-create-group\": \"true\","
          + "        \"awslogs-group\": \"/ecs/metadata\","
          + "        \"awslogs-region\": \"us-west-2\","
          + "        \"awslogs-stream\": \"ecs/curl/8f03e41243824aea923aca126495f665\""
          + "    },"
          + "    \"ContainerARN\": \"arn:aws:ecs:us-west-2:111122223333:container/0206b271-b33f-47ab-86c6-a0ba208a70a9\","
          + "    \"Networks\": ["
          + "        {"
          + "            \"NetworkMode\": \"awsvpc\","
          + "            \"IPv4Addresses\": ["
          + "                \"10.0.2.100\""
          + "            ],"
          + "            \"AttachmentIndex\": 0,"
          + "            \"MACAddress\": \"0e:9e:32:c7:48:85\","
          + "            \"IPv4SubnetCIDRBlock\": \"10.0.2.0/24\","
          + "            \"PrivateDNSName\": \"ip-10-0-2-100.us-west-2.compute.internal\","
          + "            \"SubnetGatewayIpv4Address\": \"10.0.2.1/24\""
          + "        }"
          + "    ]"
          + "}";

  private static final String METADATA_V4_TASK_RESPONSE =
      "{"
          + "    \"Cluster\": \"default\","
          + "    \"TaskARN\": \"arn:aws:ecs:us-west-2:111122223333:task/default/158d1c8083dd49d6b527399fd6414f5c\","
          + "    \"Family\": \"curltest\","
          + "    \"Revision\": \"26\","
          + "    \"DesiredStatus\": \"RUNNING\","
          + "    \"KnownStatus\": \"RUNNING\","
          + "    \"PullStartedAt\": \"2020-10-02T00:43:06.202617438Z\","
          + "    \"PullStoppedAt\": \"2020-10-02T00:43:06.31288465Z\","
          + "    \"AvailabilityZone\": \"us-west-2d\","
          + "    \"LaunchType\": \"EC2\","
          + "    \"Containers\": ["
          + "        {"
          + "            \"DockerId\": \"598cba581fe3f939459eaba1e071d5c93bb2c49b7d1ba7db6bb19deeb70d8e38\","
          + "            \"Name\": \"~internal~ecs~pause\","
          + "            \"DockerName\": \"ecs-curltest-26-internalecspause-e292d586b6f9dade4a00\","
          + "            \"Image\": \"amazon/amazon-ecs-pause:0.1.0\","
          + "            \"ImageID\": \"\","
          + "            \"Labels\": {"
          + "                \"com.amazonaws.ecs.cluster\": \"default\","
          + "                \"com.amazonaws.ecs.container-name\": \"~internal~ecs~pause\","
          + "                \"com.amazonaws.ecs.task-arn\": \"arn:aws:ecs:us-west-2:111122223333:task/default/158d1c8083dd49d6b527399fd6414f5c\","
          + "                \"com.amazonaws.ecs.task-definition-family\": \"curltest\","
          + "                \"com.amazonaws.ecs.task-definition-version\": \"26\""
          + "            },"
          + "            \"DesiredStatus\": \"RESOURCES_PROVISIONED\","
          + "            \"KnownStatus\": \"RESOURCES_PROVISIONED\","
          + "            \"Limits\": {"
          + "                \"CPU\": 0,"
          + "                \"Memory\": 0"
          + "            },"
          + "            \"CreatedAt\": \"2020-10-02T00:43:05.602352471Z\","
          + "            \"StartedAt\": \"2020-10-02T00:43:06.076707576Z\","
          + "            \"Type\": \"CNI_PAUSE\","
          + "            \"Networks\": ["
          + "                {"
          + "                    \"NetworkMode\": \"awsvpc\","
          + "                    \"IPv4Addresses\": ["
          + "                        \"10.0.2.61\""
          + "                    ],"
          + "                    \"AttachmentIndex\": 0,"
          + "                    \"MACAddress\": \"0e:10:e2:01:bd:91\","
          + "                    \"IPv4SubnetCIDRBlock\": \"10.0.2.0/24\","
          + "                    \"PrivateDNSName\": \"ip-10-0-2-61.us-west-2.compute.internal\","
          + "                    \"SubnetGatewayIpv4Address\": \"10.0.2.1/24\""
          + "                }"
          + "            ]"
          + "        },"
          + "        {"
          + "            \"DockerId\": \"ee08638adaaf009d78c248913f629e38299471d45fe7dc944d1039077e3424ca\","
          + "            \"Name\": \"curl\","
          + "            \"DockerName\": \"ecs-curltest-26-curl-a0e7dba5aca6d8cb2e00\","
          + "            \"Image\": \"111122223333.dkr.ecr.us-west-2.amazonaws.com/curltest:latest\","
          + "            \"ImageID\": \"sha256:d691691e9652791a60114e67b365688d20d19940dde7c4736ea30e660d8d3553\","
          + "            \"Labels\": {"
          + "                \"com.amazonaws.ecs.cluster\": \"default\","
          + "                \"com.amazonaws.ecs.container-name\": \"curl\","
          + "                \"com.amazonaws.ecs.task-arn\": \"arn:aws:ecs:us-west-2:111122223333:task/default/158d1c8083dd49d6b527399fd6414f5c\","
          + "                \"com.amazonaws.ecs.task-definition-family\": \"curltest\","
          + "                \"com.amazonaws.ecs.task-definition-version\": \"26\""
          + "            },"
          + "            \"DesiredStatus\": \"RUNNING\","
          + "            \"KnownStatus\": \"RUNNING\","
          + "            \"Limits\": {"
          + "                \"CPU\": 10,"
          + "                \"Memory\": 128"
          + "            },"
          + "            \"CreatedAt\": \"2020-10-02T00:43:06.326590752Z\","
          + "            \"StartedAt\": \"2020-10-02T00:43:06.767535449Z\","
          + "            \"Type\": \"NORMAL\","
          + "            \"LogDriver\": \"awslogs\","
          + "            \"LogOptions\": {"
          + "                \"awslogs-create-group\": \"true\","
          + "                \"awslogs-group\": \"/ecs/metadata\","
          + "                \"awslogs-region\": \"us-west-2\","
          + "                \"awslogs-stream\": \"ecs/curl/158d1c8083dd49d6b527399fd6414f5c\""
          + "            },"
          + "            \"ContainerARN\": \"arn:aws:ecs:us-west-2:111122223333:container/abb51bdd-11b4-467f-8f6c-adcfe1fe059d\","
          + "            \"Networks\": ["
          + "                {"
          + "                    \"NetworkMode\": \"awsvpc\","
          + "                    \"IPv4Addresses\": ["
          + "                        \"10.0.2.61\""
          + "                    ],"
          + "                    \"AttachmentIndex\": 0,"
          + "                    \"MACAddress\": \"0e:10:e2:01:bd:91\","
          + "                    \"IPv4SubnetCIDRBlock\": \"10.0.2.0/24\","
          + "                    \"PrivateDNSName\": \"ip-10-0-2-61.us-west-2.compute.internal\","
          + "                    \"SubnetGatewayIpv4Address\": \"10.0.2.1/24\""
          + "                }"
          + "            ]"
          + "        }"
          + "    ]"
          + "}";

  @RegisterExtension public static MockWebServerExtension server = new MockWebServerExtension();

  @Mock private DockerHelper mockDockerHelper;

  @Test
  void testCreateAttributesMetadataV3() throws UnknownHostException {
    when(mockDockerHelper.getContainerId()).thenReturn("0123456789A");
    Map<String, String> mockSysEnv = new HashMap<>();
    mockSysEnv.put(ECS_METADATA_KEY_V3, "ecs_metadata_v3_uri");
    Resource resource = EcsResource.buildResource(mockSysEnv, mockDockerHelper);
    Attributes attributes = resource.getAttributes();

    assertThat(resource.getSchemaUrl()).isEqualTo(ResourceAttributes.SCHEMA_URL);
    assertThat(attributes)
        .containsOnly(
            entry(ResourceAttributes.CLOUD_PROVIDER, "aws"),
            entry(ResourceAttributes.CLOUD_PLATFORM, "aws_ecs"),
            entry(ResourceAttributes.CONTAINER_NAME, InetAddress.getLocalHost().getHostName()),
            entry(ResourceAttributes.CONTAINER_ID, "0123456789A"));
  }

  @Test
  void testCreateAttributesMetadataV4() throws UnknownHostException {
    when(mockDockerHelper.getContainerId()).thenReturn("0123456789A");

    server.enqueue(HttpResponse.of(MediaType.JSON_UTF_8, METADATA_V4_RESPONSE));
    server.enqueue(HttpResponse.of(MediaType.JSON_UTF_8, METADATA_V4_TASK_RESPONSE));

    Map<String, String> mockSysEnv = new HashMap<>();
    mockSysEnv.put(ECS_METADATA_KEY_V3, "http://ecs_metadata_v3_uri");
    mockSysEnv.put(ECS_METADATA_KEY_V4, "http://localhost:" + server.httpPort());

    Resource resource = EcsResource.buildResource(mockSysEnv, mockDockerHelper);
    Attributes attributes = resource.getAttributes();

    assertThat(resource.getSchemaUrl()).isEqualTo(ResourceAttributes.SCHEMA_URL);
    assertThat(attributes)
        .containsOnly(
            entry(ResourceAttributes.CLOUD_PROVIDER, "aws"),
            entry(ResourceAttributes.CLOUD_PLATFORM, "aws_ecs"),
            entry(ResourceAttributes.CONTAINER_NAME, InetAddress.getLocalHost().getHostName()),
            entry(ResourceAttributes.CONTAINER_ID, "0123456789A"),
            entry(
                ResourceAttributes.AWS_ECS_CLUSTER_ARN,
                "arn:aws:ecs:us-west-2:111122223333:cluster/default"),
            entry(
                ResourceAttributes.AWS_ECS_CONTAINER_ARN,
                "arn:aws:ecs:us-west-2:111122223333:container/0206b271-b33f-47ab-86c6-a0ba208a70a9"),
            entry(ResourceAttributes.AWS_ECS_LAUNCHTYPE, "EC2"),
            entry(
                ResourceAttributes.AWS_ECS_TASK_ARN,
                "arn:aws:ecs:us-west-2:111122223333:task/default/158d1c8083dd49d6b527399fd6414f5c"),
            entry(ResourceAttributes.AWS_ECS_TASK_FAMILY, "curltest"),
            entry(ResourceAttributes.AWS_ECS_TASK_REVISION, "26"));
  }

  @Test
  void testNotOnEcs() {
    Map<String, String> mockSysEnv = new HashMap<>();
    mockSysEnv.put(ECS_METADATA_KEY_V3, "");
    mockSysEnv.put(ECS_METADATA_KEY_V4, "");
    Attributes attributes = EcsResource.buildResource(mockSysEnv, mockDockerHelper).getAttributes();
    assertThat(attributes).isEmpty();
  }

  @Test
  void testContainerIdMissing() throws UnknownHostException {
    when(mockDockerHelper.getContainerId()).thenReturn("");
    Map<String, String> mockSysEnv = new HashMap<>();
    mockSysEnv.put(ECS_METADATA_KEY_V3, "ecs_metadata_v3_uri");
    Attributes attributes = EcsResource.buildResource(mockSysEnv, mockDockerHelper).getAttributes();
    assertThat(attributes)
        .containsOnly(
            entry(ResourceAttributes.CLOUD_PROVIDER, "aws"),
            entry(ResourceAttributes.CLOUD_PLATFORM, "aws_ecs"),
            entry(ResourceAttributes.CONTAINER_NAME, InetAddress.getLocalHost().getHostName()));
  }

  @Test
  void inServiceLoader() {
    // No practical way to test the attributes themselves so at least check the service loader picks
    // it up.
    assertThat(ServiceLoader.load(ResourceProvider.class))
        .anyMatch(EcsResourceProvider.class::isInstance);
  }
}
