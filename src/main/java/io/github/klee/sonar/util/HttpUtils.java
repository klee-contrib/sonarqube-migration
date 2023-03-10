package io.github.klee.sonar.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * @author Kévin Buntrock
 */
public class HttpUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

    private static SSLContext ACCEPT_ALL = null;

    static {
        TrustManager[] acceptAllTrustManager = new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] xcs, String string) {
                    }

                    public void checkServerTrusted(X509Certificate[] xcs, String string) {
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }
        };
        try {
            ACCEPT_ALL = SSLContext.getInstance("ssl");
            ACCEPT_ALL.init(null, acceptAllTrustManager, null);
        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            //LOGGER.error("Error while creating a \"accept all certificate\" ssl context.", ex);
            throw new RuntimeException("Error while creating a \"accept all certificate\" ssl context.");
        }
    }

    public static SSLContext getAcceptAllSslContext() {
        return ACCEPT_ALL;
    }
}
