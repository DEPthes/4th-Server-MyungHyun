package org.depth.web.http.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Builder
@Getter
public class HttpHeader {
    private final String name;
    private final String value;

    public static HttpHeader of(String name, String value) {
        return new HttpHeader(name, value);
    }
}
