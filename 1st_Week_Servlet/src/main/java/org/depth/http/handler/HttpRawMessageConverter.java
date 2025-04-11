package org.depth.http.handler;

import org.depth.http.HttpRequest;
import org.depth.http.HttpResponse;
import org.depth.http.model.HttpHeader;

public class HttpRawMessageConverter {
    public static String toRawRequest(HttpRequest request) {
        StringBuilder rawRequest = new StringBuilder();
        rawRequest.append(request.getMethod()).append(" ")
                .append(request.getPath()).append(" ")
                .append(request.getVersion()).append("\r\n");

        for (HttpHeader header : request.getHeaders()) {
            rawRequest.append(header.getName()).append(": ")
                    .append(header.getValue()).append("\r\n");
        }

        rawRequest.append("\r\n").append(request.getBody());
        return rawRequest.toString();
    }

    public static String toRawResponse(HttpResponse response) {
        StringBuilder rawResponse = new StringBuilder();
        rawResponse.append(response.getVersion()).append(" ")
                .append(response.getStatusCode()).append(" ")
                .append(response.getStatusText()).append("\r\n");

        for (HttpHeader header : response.getHeaders()) {
            rawResponse.append(header.getName()).append(": ")
                    .append(header.getValue()).append("\r\n");
        }

        rawResponse.append("\r\n").append(response.getBody());
        return rawResponse.toString();
    }
}
