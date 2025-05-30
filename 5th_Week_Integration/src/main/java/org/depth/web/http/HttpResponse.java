package org.depth.web.http;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.depth.web.http.handler.HttpRawMessageConverter;
import org.depth.web.http.model.HttpHeader;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public abstract class HttpResponse {
    private String version;
    private int statusCode;
    private String statusText;
    private List<HttpHeader> headers;
    private String body;

    public String toRawResponse() {
        return HttpRawMessageConverter.toRawResponse(this);
    }

    public void addHeader(String name, String value) {
        this.headers.add(HttpHeader.of(name, value));
    }

    public void removeHeader(HttpHeader header) {
        this.headers.remove(header);
    }

    public void removeHeaderByName(String name) {
        this.headers.removeIf(header -> header.getName().equalsIgnoreCase(name));
    }
}
