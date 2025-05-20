package io.opentelemetry.sdk.internal;

/**
 * A {@link ComponentId} where the component type is one of {@link ComponentId.StandardExporterType}.
 * <p>
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public class StandardComponentId extends ComponentId.Lazy {

  private final StandardExporterType standardType;

  StandardComponentId(StandardExporterType standardType) {
    super(standardType.value);
    this.standardType = standardType;
  }

  public StandardExporterType getStandardType() {
    return standardType;
  }
}
