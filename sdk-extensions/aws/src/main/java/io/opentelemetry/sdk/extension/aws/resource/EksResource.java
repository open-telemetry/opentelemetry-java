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
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A factory for a {@link Resource} which provides information about the current ECS container if
 * running on AWS EKS.
 */
public final class EksResource {
  private static final Logger logger = Logger.getLogger(EksResource.class.getName());

  private static final JsonFactory JSON_FACTORY = new JsonFactory();

  static final String K8S_SVC_URL = "https://kubernetes.default.svc";
  static final String AUTH_CONFIGMAP_PATH = "/api/v1/namespaces/kube-system/configmaps/aws-auth";
  static final String CW_CONFIGMAP_PATH =
      "/api/v1/namespaces/amazon-cloudwatch/configmaps/cluster-info";
  private static final String K8S_TOKEN_PATH =
      "/var/run/secrets/kubernetes.io/serviceaccount/token";
  private static final String K8S_CERT_PATH =
      "/var/run/secrets/kubernetes.io/serviceaccount/ca.crt";

  private static final Resource INSTANCE = buildResource();

  /**
   * Returns a factory for a {@link Resource} which provides information about the current ECS
   * container if running on AWS EKS.
   */
  public static Resource get() {
    return INSTANCE;
  }

  private static Resource buildResource() {
    return buildResource(new SimpleHttpClient(), new DockerHelper(), K8S_TOKEN_PATH, K8S_CERT_PATH);
  }

  // Visible for testing
  static Resource buildResource(
      SimpleHttpClient httpClient,
      DockerHelper dockerHelper,
      String k8sTokenPath,
      String k8sKeystorePath) {
    if (!isEks(k8sTokenPath, k8sKeystorePath, httpClient)) {
      return Resource.empty();
    }

    AttributesBuilder attrBuilders = Attributes.builder();
    attrBuilders.put(ResourceAttributes.CLOUD_PROVIDER, ResourceAttributes.CloudProviderValues.AWS);
    attrBuilders.put(
        ResourceAttributes.CLOUD_PLATFORM, ResourceAttributes.CloudPlatformValues.AWS_EKS);

    String clusterName = getClusterName(httpClient);
    if (clusterName != null && !clusterName.isEmpty()) {
      attrBuilders.put(ResourceAttributes.K8S_CLUSTER_NAME, clusterName);
    }

    String containerId = dockerHelper.getContainerId();
    if (containerId != null && !containerId.isEmpty()) {
      attrBuilders.put(ResourceAttributes.CONTAINER_ID, containerId);
    }

    return Resource.create(attrBuilders.build(), ResourceAttributes.SCHEMA_URL);
  }

  private static boolean isEks(
      String k8sTokenPath, String k8sKeystorePath, SimpleHttpClient httpClient) {
    if (!isK8s(k8sTokenPath, k8sKeystorePath)) {
      logger.log(Level.FINE, "Not running on k8s.");
      return false;
    }

    Map<String, String> requestProperties = new HashMap<>();
    requestProperties.put("Authorization", getK8sCredHeader());
    String awsAuth =
        httpClient.fetchString(
            "GET", K8S_SVC_URL + AUTH_CONFIGMAP_PATH, requestProperties, K8S_CERT_PATH);

    return awsAuth != null && !awsAuth.isEmpty();
  }

  private static boolean isK8s(String k8sTokenPath, String k8sKeystorePath) {
    File k8sTokeyFile = new File(k8sTokenPath);
    File k8sKeystoreFile = new File(k8sKeystorePath);
    return k8sTokeyFile.exists() && k8sKeystoreFile.exists();
  }

  private static String getClusterName(SimpleHttpClient httpClient) {
    Map<String, String> requestProperties = new HashMap<>();
    requestProperties.put("Authorization", getK8sCredHeader());
    String json =
        httpClient.fetchString(
            "GET", K8S_SVC_URL + CW_CONFIGMAP_PATH, requestProperties, K8S_CERT_PATH);

    try (JsonParser parser = JSON_FACTORY.createParser(json)) {
      parser.nextToken();

      if (!parser.isExpectedStartObjectToken()) {
        throw new IOException("Invalid JSON:" + json);
      }

      while (parser.nextToken() != JsonToken.END_OBJECT) {
        parser.nextToken();
        if (!parser.getCurrentName().equals("data")) {
          parser.skipChildren();
          continue;
        }

        if (!parser.isExpectedStartObjectToken()) {
          throw new IOException("Invalid JSON:" + json);
        }

        while (parser.nextToken() != JsonToken.END_OBJECT) {
          String value = parser.nextTextValue();
          if (!parser.getCurrentName().equals("cluster.name")) {
            parser.skipChildren();
            continue;
          }
          return value;
        }
      }
    } catch (IOException e) {
      logger.log(Level.WARNING, "Can't get cluster name on EKS.", e);
    }
    return "";
  }

  private static String getK8sCredHeader() {
    try {
      String content =
          new String(Files.readAllBytes(Paths.get(K8S_TOKEN_PATH)), StandardCharsets.UTF_8);
      return "Bearer " + content;
    } catch (IOException e) {
      logger.log(Level.WARNING, "Unable to load K8s client token.", e);
    }
    return "";
  }

  private EksResource() {}
}
