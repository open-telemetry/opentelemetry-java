package io.opentelemetry.exporter.internal.marshal;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

class ProtoSerializerTest {

  @Test
  void testSerializeStringWithContext() throws Exception {
    String value = "mystring";
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    ProtoSerializer serializer = new ProtoSerializer(out);

    ProtoFieldInfo field = ProtoFieldInfo.create(1, 10, "stringValue");
    MarshalerContext context = new MarshalerContext();
    context.setSize(0, value.length());
    serializer.serializeStringWithContext(field, value, context);
    serializer.close();

    byte[] result = out.toByteArray();
    assertThat(result.length).isEqualTo(10); // one byte tag, one byte length, rest string
    assertThat((int)result[0]).isEqualTo(field.getTag());
    assertThat((int)result[1]).isEqualTo(value.length());
    assertThat(new String(result, 2, result.length-2, UTF_8)).isEqualTo(value);
  }

  @Test
  void testSerializeEmptyStringWithContext() throws Exception {
    String value = "";
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    ProtoSerializer serializer = new ProtoSerializer(out);

    ProtoFieldInfo field = ProtoFieldInfo.create(1, 10, "stringValue");
    MarshalerContext context = new MarshalerContext();
    context.setSize(0, 0);

    serializer.serializeStringWithContext(field, value, context);
    serializer.close();

    byte[] result = out.toByteArray();

    assertThat(result.length).isEqualTo(2);
    assertThat((int)result[0]).isEqualTo(field.getTag());
    assertThat((int)result[1]).isEqualTo(0);
  }

}
