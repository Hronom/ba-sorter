package com.github.hronom.ba.sorter.controllers;

import com.github.hronom.ba.sorter.config.custom.objects.CustomUser;
import com.github.hronom.ba.sorter.services.OkHttpClientFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Principal;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;

@Controller
public class ProxyController {

    @Autowired
    private OkHttpClientFactory okHttpClientFactory;

    @RequestMapping(value = "/**", consumes = MediaType.ALL_VALUE)
    public @ResponseBody
    ResponseEntity<byte[]> mirrorRest(
        @RequestBody(required = false) String body,
        HttpMethod method,
        HttpServletRequest request,
        HttpServletResponse response,
        Principal principal
    ) throws URISyntaxException, IOException {
        CustomUser user = (CustomUser) principal;
        if (user != null) {
            OkHttpClient client = okHttpClientFactory.getClient(user.getUrl());
            return exchange(user.getUrl(), method, request, body, client);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    private ResponseEntity<byte[]> exchange(
        String serverSpec,
        HttpMethod method,
        HttpServletRequest httpServletRequest,
        final String body,
        OkHttpClient client
    ) throws IOException, URISyntaxException {
        URL serverURL = new URL(serverSpec);

        HttpUrl.Builder httpUrlBuilder = new HttpUrl.Builder();
        httpUrlBuilder.scheme(serverURL.getProtocol());
        httpUrlBuilder.host(serverURL.getHost());
        httpUrlBuilder.encodedPath(httpServletRequest.getRequestURI());
        if (httpServletRequest.getQueryString() != null) {
            httpUrlBuilder.query(httpServletRequest.getQueryString());
        }
        HttpUrl httpUrl = httpUrlBuilder.build();

        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(httpUrl);
        switch (method){
            case GET:
                requestBuilder.get();
                break;
            case HEAD:
                requestBuilder.head();
                break;
            case POST:
                requestBuilder.post(createOkhttp3RequestBody(httpServletRequest.getContentType(), body));
                break;
            case PUT:
                requestBuilder.put(createOkhttp3RequestBody(httpServletRequest.getContentType(), body));
                break;
            case PATCH:
                requestBuilder.patch(createOkhttp3RequestBody(httpServletRequest.getContentType(), body));
                break;
            case DELETE:
                if (body != null) {
                    requestBuilder.delete(createOkhttp3RequestBody(httpServletRequest.getContentType(), body));
                } else {
                    requestBuilder.delete();
                }
                break;
            case OPTIONS:
                System.out.println("OPTIONS");
                break;
            case TRACE:
                System.out.println("TRACE");
                break;
        }

        Headers.Builder requestHeadersBuilder = new Headers.Builder();
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if ("host".equalsIgnoreCase(headerName)) {
               // requestHeadersBuilder.add(headerName, serverURL.getHost());
                System.out.println("host, skeep");
            } else if ("authorization".equalsIgnoreCase(headerName)) {
                System.out.println("authorization, skeep");
            } else {
                Enumeration<String> headerValues = httpServletRequest.getHeaders(headerName);
                while (headerValues.hasMoreElements()) {
                    String value = headerValues.nextElement();
                    requestHeadersBuilder.add(headerName, value);
                }
            }
        }

        Headers requestHeaders = requestHeadersBuilder.build();
        Request request = requestBuilder.headers(requestHeaders).build();

        Response response = client.newCall(request).execute();

        HttpHeaders responseHeaders = new HttpHeaders();
        for (Map.Entry<String, List<String>> header : response.headers().toMultimap().entrySet()) {
            if (header.getKey().equals("Transfer-Encoding") ||
                header.getValue().contains("chunked")) {
                System.out.println("Chunked");
            } else {
                responseHeaders.put(header.getKey(), header.getValue());
            }
        }

        return new ResponseEntity<>(
            response.body().bytes(),
            responseHeaders,
            HttpStatus.valueOf(response.code())
        );
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