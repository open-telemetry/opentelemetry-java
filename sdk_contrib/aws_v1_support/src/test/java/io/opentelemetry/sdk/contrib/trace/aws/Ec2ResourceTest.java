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

package io.opentelemetry.sdk.contrib.trace.aws;

import static com.google.common.truth.Truth.assertThat;

import com.amazonaws.util.EC2MetadataUtils.InstanceInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Ec2Resource}. */
@RunWith(JUnit4.class)
public class Ec2ResourceTest {

  @Test
  public void shouldReturnResourceWithOnlyCloudProviderLabelIfNotRunningOnEc2() {
    Resource resource = Ec2Resource.getResourceFromInfoAndHost(null, null);
    assertThat(resource.getLabels().get(ResourceConstants.CLOUD_PROVIDER))
        .isEqualTo(Ec2Resource.CLOUD_PROVIDER_AWS);
  }

  @Test
  public void shouldReturnResourceWithValuesFromEc2MetadataEndpointIfAvailable() {
    String pendingTime = "0.0";
    String instanceType = "t2.micro";
    String imageId = "ami-00eb20669e0990cb4";
    String instanceId = "i-06b058d0aea36e96e";
    String[] billingProducts = new String[0];
    String architecture = "x86_64 HVM gp2";
    String accountId = "123456789012";
    String kernelId = "not available";
    String ramdiskId = "not available";
    String region = "us-east-1";
    String version = "2018.03.0.20190826";
    String availabilityZone = "us-east-1a";
    String privateIp = "172.31.30.166";
    String[] devpayProductCodes = new String[0];
    InstanceInfo instanceInfo =
        new InstanceInfo(
            pendingTime,
            instanceType,
            imageId,
            instanceId,
            billingProducts,
            architecture,
            accountId,
            kernelId,
            ramdiskId,
            region,
            version,
            availabilityZone,
            privateIp,
            devpayProductCodes);
    String hostname = "ip-172-31-30-166.ec2.internal";
    Resource resource = Ec2Resource.getResourceFromInfoAndHost(instanceInfo, hostname);
    assertThat(resource.getLabels().get(ResourceConstants.CLOUD_PROVIDER))
        .isEqualTo(Ec2Resource.CLOUD_PROVIDER_AWS);
    assertThat(resource.getLabels().get(ResourceConstants.CLOUD_ACCOUNT)).isEqualTo(accountId);
    assertThat(resource.getLabels().get(ResourceConstants.CLOUD_REGION)).isEqualTo(region);
    assertThat(resource.getLabels().get(ResourceConstants.CLOUD_ZONE)).isEqualTo(availabilityZone);
    assertThat(resource.getLabels().get(ResourceConstants.HOST_ID)).isEqualTo(instanceId);
    assertThat(resource.getLabels().get(ResourceConstants.HOST_NAME)).isEqualTo(privateIp);
    assertThat(resource.getLabels().get(ResourceConstants.HOST_TYPE)).isEqualTo(instanceType);
    assertThat(resource.getLabels().get(ResourceConstants.HOST_HOSTNAME)).isEqualTo(hostname);
    assertThat(resource.getLabels().get(ResourceConstants.HOST_IMAGE_ID)).isEqualTo(imageId);
  }
}
