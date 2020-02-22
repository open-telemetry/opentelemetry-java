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

import static com.google.common.base.Strings.isNullOrEmpty;

import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceConstants;
import io.opentelemetry.trace.AttributeValue;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;

/** Factory bean for service resource labels. */
class ServiceResourceFactory implements FactoryBean<Resource> {

  private static final String SEMVER = "semver:";
  private static final String GIT = "git:";
  private static final Pattern SEMVER_PATTERN =
      Pattern.compile(
          "^([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:-([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?"
              + "(?:\\+[0-9A-Za-z-]+)?$");

  @Nullable private final String applicationName;
  @Nullable private final BuildProperties buildProperties;
  @Nullable private final GitProperties gitProperties;

  ServiceResourceFactory(
      @Nullable String applicationName,
      @Nullable BuildProperties buildProperties,
      @Nullable GitProperties gitProperties) {
    this.applicationName = applicationName;
    this.buildProperties = buildProperties;
    this.gitProperties = gitProperties;
  }

  @Override
  public Resource getObject() {
    Map<String, AttributeValue> labels = new HashMap<>();
    populateLabelsFromPropertySourcesIfAvailable(labels);
    populateLabelsWithBuildInfoIfAvailable(labels);
    populateLabelsWithGitInfoIfAvailable(labels);
    return Resource.create(labels);
  }

  @Override
  public Class<?> getObjectType() {
    return Resource.class;
  }

  private void populateLabelsFromPropertySourcesIfAvailable(Map<String, AttributeValue> labels) {
    if (!isNullOrEmpty(applicationName)) {
      labels.put(
          ResourceConstants.SERVICE_NAME, AttributeValue.stringAttributeValue(applicationName));
    }
  }

  private void populateLabelsWithBuildInfoIfAvailable(Map<String, AttributeValue> labels) {
    if (buildProperties != null) {
      if (!isNullOrEmpty(buildProperties.getGroup())) {
        labels.put(
            ResourceConstants.SERVICE_NAMESPACE,
            AttributeValue.stringAttributeValue(buildProperties.getGroup()));
      }
      if (!isNullOrEmpty(buildProperties.getArtifact())) {
        labels.put(
            ResourceConstants.SERVICE_NAME,
            AttributeValue.stringAttributeValue(buildProperties.getArtifact()));
      }
      String version = buildProperties.getVersion();
      if (!isNullOrEmpty(version)) {
        Matcher matcher = SEMVER_PATTERN.matcher(version);
        if (matcher.find()) {
          labels.put(
              ResourceConstants.SERVICE_VERSION,
              AttributeValue.stringAttributeValue(SEMVER + buildProperties.getVersion()));
        } else {
          labels.put(
              ResourceConstants.SERVICE_VERSION,
              AttributeValue.stringAttributeValue(buildProperties.getVersion()));
        }
      }
    }
  }

  private void populateLabelsWithGitInfoIfAvailable(Map<String, AttributeValue> labels) {
    if (gitProperties != null) {
      if (!isNullOrEmpty(gitProperties.getShortCommitId())) {
        labels.put(
            ResourceConstants.SERVICE_VERSION,
            AttributeValue.stringAttributeValue(GIT + gitProperties.getShortCommitId()));
      }
    }
  }
}
