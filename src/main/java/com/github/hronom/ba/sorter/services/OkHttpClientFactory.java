package com.github.hronom.ba.sorter.services;

import com.github.hronom.ba.sorter.controllers.LoggingInterceptor;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okio.BufferedSink;

@Service
public class OkHttpClientFactory {
    //private final Object clientsLock
    private final ConcurrentHashMap<String, OkHttpClient> clientsByUrl = new ConcurrentHashMap<>();

    public OkHttpClient getClient(String url) {
        return clientsByUrl.computeIfAbsent(url, k -> getUnsafeOkHttpClient());
    }

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            X509TrustManager x509TrustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType
                ) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType
                ) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[] {};
                }
            };

            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {x509TrustManager};

            // Install the all-trusting trust manager
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, x509TrustManager);
            builder.followRedirects(true);
            builder.followSslRedirects(true);
            builder.addNetworkInterceptor(new LoggingInterceptor());
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return builder.build();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private okhttp3.RequestBody createOkhttp3RequestBody(String contentType, String body) {
        return new okhttp3.RequestBody() {
            @Override
            public okhttp3.MediaType contentType() {
                return okhttp3.MediaType.parse(contentType);
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                if (body != null) {
                    sink.write(body.getBytes());
                }
            }
        };
    }
}