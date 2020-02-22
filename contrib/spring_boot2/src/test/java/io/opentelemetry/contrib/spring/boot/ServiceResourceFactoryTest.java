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

package io.opentelemetry.contrib.spring.boot;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceConstants;
import java.util.Properties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;

/** Unit tests for {@link ServiceResourceFactory}. */
@RunWith(JUnit4.class)
public class ServiceResourceFactoryTest {

  @Test
  public void shouldUseSpringApplicationNameIfInfoPropertiesNotPresent() {
    String appName = "junit";
    ServiceResourceFactory factory = new ServiceResourceFactory(appName, null, null);
    Resource resource = factory.getObject();
    assertThat(resource.getLabels().get(ResourceConstants.SERVICE_NAME).getStringValue())
        .isEqualTo(appName);
  }

  @Test
  public void shouldReturnEmptyLabelsIfNoConstructorObjectsSupplied() {
    String appName = null;
    ServiceResourceFactory factory = new ServiceResourceFactory(appName, null, null);
    Resource resource = factory.getObject();
    assertThat(resource.getLabels().isEmpty()).isTrue();
  }

  @Test
  public void shouldUseBuildPropertiesIfPresent() {
    String appName = "junit";
    String group = "io.opentelemetry.contrib";
    String artifact = "spring-boot-opentelemetry-actuator";
    String version = "0.3.0";
    Properties properties = new Properties();
    properties.setProperty("group", group);
    properties.setProperty("artifact", artifact);
    properties.setProperty("version", version);
    BuildProperties buildProperties = new BuildProperties(properties);
    ServiceResourceFactory factory = new ServiceResourceFactory(appName, buildProperties, null);
    Resource resource = factory.getObject();
    assertThat(resource.getLabels().get(ResourceConstants.SERVICE_NAME).getStringValue())
        .isEqualTo(artifact);
    assertThat(resource.getLabels().get(ResourceConstants.SERVICE_NAMESPACE).getStringValue())
        .isEqualTo(group);
    assertThat(resource.getLabels().get(ResourceConstants.SERVICE_VERSION).getStringValue())
        .isEqualTo("semver:" + version);
  }

  @Test
  public void shouldUseGitCommitForVersionIfPresent() {
    String appName = "junit";
    String group = "io.opentelemetry.contrib";
    String artifact = "spring-boot-opentelemetry-actuator";
    String version = "Redstone.SR1";
    String gitHash = "7417ca14a757";
    Properties bldProps = new Properties();
    bldProps.setProperty("group", group);
    bldProps.setProperty("artifact", artifact);
    bldProps.setProperty("version", version);
    BuildProperties buildProperties = new BuildProperties(bldProps);
    Properties gitProps = new Properties();
    gitProps.setProperty("commit.id.abbrev", gitHash);
    GitProperties gitProperties = new GitProperties(gitProps);
    ServiceResourceFactory factory =
        new ServiceResourceFactory(appName, buildProperties, gitProperties);
    Resource resource = factory.getObject();
    assertThat(resource.getLabels().get(ResourceConstants.SERVICE_NAME).getStringValue())
        .isEqualTo(artifact);
    assertThat(resource.getLabels().get(ResourceConstants.SERVICE_NAMESPACE).getStringValue())
        .isEqualTo(group);
    assertThat(resource.getLabels().get(ResourceConstants.SERVICE_VERSION).getStringValue())
        .isEqualTo("git:" + gitHash);
  }
}
