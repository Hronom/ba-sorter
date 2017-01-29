package com.github.hronom.ba.sorter.handlers;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

public class ClientErrorHandler implements ResponseErrorHandler {
    @Override
    public void handleError(ClientHttpResponse response) throws IOException {


    }

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return false;
    }
}
