package io.opentelemetry.common;

import java.util.HashMap;

public class ReadWriteLabels extends HashMap<String, String> {
  private static final long serialVersionUID = 362498820763181265L;


  public ReadWriteLabels(Labels labels) {
    super();
    labels.forEach(this::put);
  }

  public Labels toLabels() {
    Labels.Builder b = Labels.newBuilder();
    this.entrySet().stream().forEach(e -> b.setLabel(e.getKey(), e.getValue()));
    return b.build();
  }

}
