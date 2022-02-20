package io.opentelemetry.exporter.internal;

import org.junit.jupiter.api.Test;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.io.InputStream;

public class TlsUtilsTest {

  @Test
  void createSslSocketFactoryTest() throws IOException {
    String filePath = this.getClass().getClassLoader().getResource("exampleClientKey.pem")
        .getFile();
    byte[][] clientKeys = TlsUtil.loadPemFile(filePath);
    InputStream isCert = this.getClass().getClassLoader().getResourceAsStream("exampleServer.pem");
    byte[] trustedCerts = new byte[isCert.available()];
    isCert.read(trustedCerts);
    KeyManager km = TlsUtil.keyManager(clientKeys);
    TrustManager tm = TlsUtil.trustManager(trustedCerts);
    TlsUtil.sslSocketFactory(km, tm);
  }

}
