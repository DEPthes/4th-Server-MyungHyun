package org.depth.http.handler;

import java.util.ArrayList;

import org.depth.servlet.http.HttpServletResponse;


public class HttpResponseWriter {

  public static HttpServletResponse createDefaultResponse() {
    return HttpServletResponse.builder()
        .version("HTTP/1.1")
        .statusCode(200)
        .statusText("OK")
        .headers(new ArrayList<>())
        .body("")
        .build();
  }


  public static void standardizeResponse(HttpServletResponse response) {
    // 응답 헤더가 null인 경우 초기화
    if (response.getHeaders() == null) {
      response.setHeaders(new ArrayList<>());
    }

    // Content-Type 헤더가 없으면 기본값 설정
    if (response.getHeaders().stream().noneMatch(h -> h.getName().equalsIgnoreCase("Content-Type"))) {
      response.addHeader("Content-Type", "text/html; charset=UTF-8");
    }

    // Content-Length 헤더 재설정
    response.removeHeaderByName("Content-Length");
    if (response.getBody() != null) {
      response.addHeader("Content-Length", String.valueOf(response.getBody().getBytes().length));
    }

    // Server 헤더 추가
    if (response.getHeaders().stream().noneMatch(h -> h.getName().equalsIgnoreCase("Server"))) {
      response.addHeader("Server", "Depth-SimpleServletContainer");
    }
  }


  public static HttpServletResponse createNotFoundResponse(String path) {
    HttpServletResponse response = createDefaultResponse();
    response.setStatusCode(404);
    response.setStatusText("Not Found");
    response.setBody("<html><body><h1>404 Not Found</h1><p>The requested URL " + path
        + " was not found on this server.</p></body></html>");

    standardizeResponse(response);
    return response;
  }


  public static HttpServletResponse createServerErrorResponse(String errorMessage) {
    HttpServletResponse response = createDefaultResponse();
    response.setStatusCode(500);
    response.setStatusText("Internal Server Error");
    response.setBody("<html><body><h1>500 Internal Server Error</h1><p>" + errorMessage + "</p></body></html>");

    standardizeResponse(response);
    return response;
  }
}