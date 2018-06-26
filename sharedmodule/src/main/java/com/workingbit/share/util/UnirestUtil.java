package com.workingbit.share.util;

import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToData;

/**
 * Created by Aleksey Popryaduhin on 00:02 28/09/2017.
 */
public class UnirestUtil {

  public static void configureSerialization() {
    try {
      SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy() {
        public boolean isTrusted(X509Certificate[] chain, String authType) {
          return true;
        }
      }).build();

      int timeout = 3 * 60;
      RequestConfig config = RequestConfig.custom()
          .setConnectTimeout(timeout * 1000)
          .setConnectionRequestTimeout(timeout * 1000)
          .setSocketTimeout(timeout * 1000).build();
      HttpClient unsafeHttpClient = HttpClients.custom().setSSLContext(sslContext)
          .setDefaultRequestConfig(config)
          .setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
      Unirest.setHttpClient(unsafeHttpClient);
    } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
      e.printStackTrace();
    }
    Unirest.setObjectMapper(new ObjectMapper() {

      public <T> T readValue(String value, @NotNull Class<T> valueType) {
        return jsonToData(value, valueType);
      }

      public String writeValue(Object value) {
        return dataToJson(value);
      }
    });
  }
}
