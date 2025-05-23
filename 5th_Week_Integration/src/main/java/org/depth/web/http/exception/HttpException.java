package org.depth.web.http.exception;

public abstract class HttpException extends RuntimeException {
    public HttpException(String message) {
        super(message);
    }
}
