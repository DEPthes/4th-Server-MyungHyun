package org.depth.http.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.depth.http.model.HttpHeader;
import org.depth.servlet.http.HttpServletRequest;


public class HttpRequestParser {


  public static HttpServletRequest parse(BufferedReader reader) throws IOException {
    // 요청 첫 줄 읽기
    String requestLine = reader.readLine();
    if (requestLine == null) {
      throw new IOException("Invalid HTTP request: empty request");
    }

    String[] requestParts = requestLine.split(" ");
    if (requestParts.length < 3) {
      throw new IOException("Invalid HTTP request format: " + requestLine);
    }

    String method = requestParts[0];
    String path = requestParts[1];
    String version = requestParts[2];

    // 헤더 파싱
    List<HttpHeader> headers = parseHeaders(reader);

    // 요청 본문 파싱
    String body = parseBody(reader, headers);

    // HttpServletRequest 생성 및 반환
    return HttpServletRequest.builder()
        .method(method)
        .path(path)
        .version(version)
        .headers(headers)
        .body(body)
        .build();
  }


  private static List<HttpHeader> parseHeaders(BufferedReader reader) throws IOException {
    List<HttpHeader> headers = new ArrayList<>();
    String headerLine;

    while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
      String[] headerParts = headerLine.split(":", 2);
      if (headerParts.length == 2) {
        headers.add(HttpHeader.of(headerParts[0].trim(), headerParts[1].trim()));
      }
    }

    return headers;
  }

  private static String parseBody(BufferedReader reader, List<HttpHeader> headers) throws IOException {
    StringBuilder bodyBuilder = new StringBuilder();

    // Content-Length 헤더가 있는 경우에만 본문 파싱
    if (headers.stream().anyMatch(h -> h.getName().equalsIgnoreCase("Content-Length"))) {
      int contentLength = headers.stream()
          .filter(h -> h.getName().equalsIgnoreCase("Content-Length"))
          .map(h -> Integer.parseInt(h.getValue().trim()))
          .findFirst()
          .orElse(0);

      if (contentLength > 0) {
        char[] buffer = new char[contentLength];
        reader.read(buffer, 0, contentLength);
        bodyBuilder.append(buffer);
      }
    }

    return bodyBuilder.toString();
  }
}