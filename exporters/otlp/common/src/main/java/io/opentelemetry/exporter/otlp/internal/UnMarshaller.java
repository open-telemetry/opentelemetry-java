package io.opentelemetry.exporter.otlp.internal;

import java.io.IOException;
import java.io.InputStream;

/**
 * UnMarshaller from an SDK structure to protobuf wire format.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public abstract class UnMarshaller {

  public abstract void read(InputStream inputStream) throws IOException;
}
