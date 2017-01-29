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
    private final ConcurrentHashMap<String, OkHttpClient> clientsByUrl = new ConcurrentHashMap<>();

    public OkHttpClient getClient(String url) {
        OkHttpClient okHttpClient = clientsByUrl.get(url);
        if (okHttpClient == null) {
            okHttpClient = getUnsafeOkHttpClient();
            clientsByUrl.put(url, okHttpClient);
        }
        return okHttpClient;
    }

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws
                        CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory);
            builder.followRedirects(true);
            builder.followSslRedirects(true);
            builder.addNetworkInterceptor(new LoggingInterceptor());
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private okhttp3.RequestBody createOkhttp3RequestBody(
        final String contentType, final String body
    ) {
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