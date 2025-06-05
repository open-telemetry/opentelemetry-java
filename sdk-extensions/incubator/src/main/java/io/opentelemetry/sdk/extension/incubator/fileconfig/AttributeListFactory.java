/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.toList;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeNameValueModel;
import java.util.List;
import javax.annotation.Nullable;

final class AttributeListFactory implements Factory<List<AttributeNameValueModel>, Attributes> {

  private static final AttributeListFactory INSTANCE = new AttributeListFactory();

  private AttributeListFactory() {}

  static AttributeListFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public Attributes create(List<AttributeNameValueModel> model, DeclarativeConfigContext context) {
    AttributesBuilder builder = Attributes.builder();

    for (AttributeNameValueModel nameValueModel : model) {
      addToBuilder(nameValueModel, builder);
    }

    return builder.build();
  }

  private static void addToBuilder(
      AttributeNameValueModel nameValueModel, AttributesBuilder builder) {
    String name = FileConfigUtil.requireNonNull(nameValueModel.getName(), "attribute name");
    Object value = FileConfigUtil.requireNonNull(nameValueModel.getValue(), "attribute value");
    AttributeNameValueModel.AttributeType type = nameValueModel.getType();
    if (type == null) {
      type = AttributeNameValueModel.AttributeType.STRING;
    }
    switch (type) {
      case STRING:
        if (value instanceof String) {
          builder.put(name, (String) value);
          return;
        }
        break;
      case BOOL:
        if (value instanceof Boolean) {
          builder.put(name, (boolean) value);
          return;
        }
        break;
      case INT:
        if ((value instanceof Integer) || (value instanceof Long)) {
          builder.put(name, ((Number) value).longValue());
          return;
        }
        break;
      case DOUBLE:
        if (value instanceof Number) {
          builder.put(name, ((Number) value).doubleValue());
          return;
        }
        break;
      case STRING_ARRAY:
        List<String> stringList = checkListOfType(value, String.class);
        if (stringList != null) {
          builder.put(AttributeKey.stringArrayKey(name), stringList);
          return;
        }
        break;
      case BOOL_ARRAY:
        List<Boolean> boolList = checkListOfType(value, Boolean.class);
        if (boolList != null) {
          builder.put(AttributeKey.booleanArrayKey(name), boolList);
          return;
        }
        break;
      case INT_ARRAY:
        List<Long> longList = checkListOfType(value, Long.class);
        if (longList != null) {
          builder.put(AttributeKey.longArrayKey(name), longList);
          return;
        }
        List<Integer> intList = checkListOfType(value, Integer.class);
        if (intList != null) {
          builder.put(
              AttributeKey.longArrayKey(name),
              intList.stream().map(i -> (long) i).collect(toList()));
          return;
        }
        break;
      case DOUBLE_ARRAY:
        List<Double> doubleList = checkListOfType(value, Double.class);
        if (doubleList != null) {
          builder.put(AttributeKey.doubleArrayKey(name), doubleList);
          return;
        }
        List<Float> floatList = checkListOfType(value, Float.class);
        if (floatList != null) {
          builder.put(
              AttributeKey.doubleArrayKey(name),
              floatList.stream().map(i -> (double) i).collect(toList()));
          return;
        }
        break;
    }
    throw new DeclarativeConfigException(
        "Error processing attribute with name \""
            + name
            + "\": value did not match type "
            + type.name());
  }

  @SuppressWarnings("unchecked")
  @Nullable
  private static <T> List<T> checkListOfType(Object value, Class<T> expectedType) {
    if (!(value instanceof List)) {
      return null;
    }
    List<?> list = (List<?>) value;
    if (list.isEmpty()) {
      return null;
    }
    if (!list.stream().allMatch(entry -> expectedType.isAssignableFrom(entry.getClass()))) {
      return null;
    }
    return (List<T>) value;
  }
}
