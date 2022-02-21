package io.opentelemetry.exporter.internal;

import org.junit.jupiter.api.Test;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TlsUtilsTest {

  @Test
  void createSslSocketFactoryTest() throws IOException {
    String clientKeyFile = this.getClass().getClassLoader()
        .getResource("exampleClientKey.pem").getFile();
    byte[] clientKey = TlsUtil.loadPemFile(clientKeyFile);
    String clientKeyChainFile = this.getClass().getClassLoader()
        .getResource("exampleClientKeyChain.pem").getFile();
    FileInputStream fis = new FileInputStream(clientKeyChainFile);
    byte[] clientKeyChain = new byte[fis.available()];
    fis.read(clientKeyChain);
    KeyManager km = TlsUtil.keyManager(clientKey, clientKeyChain);

    InputStream isCert = this.getClass().getClassLoader()
        .getResourceAsStream("exampleServer.pem");
    byte[] trustedCerts = new byte[isCert.available()];
    isCert.read(trustedCerts);
    TrustManager tm = TlsUtil.trustManager(trustedCerts);

    TlsUtil.sslSocketFactory(km, tm);
  }

}
