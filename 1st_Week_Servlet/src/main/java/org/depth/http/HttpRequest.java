package org.depth.http;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.depth.http.handler.HttpRawMessageConverter;
import org.depth.http.model.HttpHeader;

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
