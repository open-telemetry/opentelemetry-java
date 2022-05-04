package io.opentelemetry.exporter.internal.okhttp;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class JsonRequestBody extends RequestBody {
  private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");

  private final Marshaler marshaler;
  private final int contentLength;

  /** Creates a new {@link JsonRequestBody}. */
  public JsonRequestBody(Marshaler marshaler) {
    this.marshaler = marshaler;
    contentLength = marshaler.getBinarySerializedSize();
  }

  @Override
  public long contentLength() {
    return contentLength;
  }

  @Override
  public MediaType contentType() {
    return JSON_MEDIA_TYPE;
  }

  @Override
  public void writeTo(BufferedSink bufferedSink) throws IOException {
    marshaler.writeJsonTo(bufferedSink.outputStream());
  }
}
