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
public abstract class HttpRequest {
    private String method;
    private String path;
    private String version;
    private List<HttpHeader> headers;
    private String body;

    public String toRawRequest() {
        return HttpRawMessageConverter.toRawRequest(this);
    }
}
