package org.depth.container;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.depth.http.exception.HttpException;
import org.depth.http.handler.HttpRequestParser;
import org.depth.http.handler.HttpResponseWriter;
import org.depth.servlet.Servlet;
import org.depth.servlet.http.HttpServletRequest;
import org.depth.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServletContainer {
  private final Map<String, Servlet> servletMap = new HashMap<>();
  private final int port;
  private ServerSocket serverSocket;
  private AtomicBoolean running = new AtomicBoolean(false);
  private Thread serverThread;

  public void addServlet(String path, Servlet servlet) {
    servletMap.put(path, servlet);
    servlet.init();
  }

  public Servlet removeServlet(String path) {
    Servlet servlet = servletMap.remove(path);
    if (servlet != null) {
      servlet.destroy();
    }
    return servlet;
  }


  public void start() {
    if (running.get()) {
      return; // 이미 실행중이면 무시
    }

    serverThread = new Thread(() -> {
      try {
        serverSocket = new ServerSocket(port);
        System.out.println("서블릿 컨테이너가 시작되었습니다. 포트: " + port);
        running.set(true);

        while (running.get()) {
          try {
            Socket clientSocket = serverSocket.accept();
            handleRequest(clientSocket);
          } catch (IOException e) {
            if (running.get()) {
              System.err.println("클라이언트 요청 처리 중 오류: " + e.getMessage());
            }
          }
        }
      } catch (IOException e) {
        System.err.println("서버 소켓 생성 중 오류: " + e.getMessage());
      } finally {
        closeServerSocket();
      }
    });

    serverThread.start();
  }


  public void stop() {
    if (!running.get()) {
      return; // 이미 중지되었으면 무시
    }

    running.set(false);
    closeServerSocket();

    // 모든 서블릿 destroy 호출
    for (Servlet servlet : servletMap.values()) {
      try {
        servlet.destroy();
      } catch (Exception e) {
        System.err.println("서블릿 종료 중 오류: " + e.getMessage());
      }
    }

    System.out.println("서블릿 컨테이너가 중지되었습니다.");
  }


  private void handleRequest(Socket clientSocket) {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        OutputStream outputStream = clientSocket.getOutputStream()) {

      // HTTP 요청 파싱
      HttpServletRequest request = HttpRequestParser.parse(reader);
      HttpServletResponse response = HttpResponseWriter.createDefaultResponse();

      // 요청 경로에 맞는 서블릿 찾기
      String path = request.getPath();
      Servlet servlet = findServletForPath(path);

      if (servlet != null) {
        // 서블릿 실행
        try {
          servlet.service(request, response);
        } catch (Exception e) {
          System.err.println("서블릿 실행 중 오류: " + e.getMessage());
          response = HttpResponseWriter.createServerErrorResponse("서블릿 실행 중 오류가 발생했습니다: " + e.getMessage());
        }
      } else {
        // 적절한 서블릿이 없을 경우 404 오류
        response = HttpResponseWriter.createNotFoundResponse(path);
      }

      // 표준 응답 헤더 추가
      HttpResponseWriter.standardizeResponse(response);

      // 응답 전송
      outputStream.write(response.getContent());
      outputStream.flush();

    } catch (IOException e) {
      System.err.println("요청 처리 중 I/O 오류: " + e.getMessage());
    } catch (HttpException e) {
      System.err.println("HTTP 파싱 오류: " + e.getMessage());
    } catch (Exception e) {
      System.err.println("요청 처리 중 오류: " + e.getMessage());
    } finally {
      try {
        clientSocket.close();
      } catch (IOException e) {
        System.err.println("소켓 닫기 오류: " + e.getMessage());
      }
    }
  }


  private Servlet findServletForPath(String path) {
    // 정확히 일치하는 경로가 있는지 확인
    if (servletMap.containsKey(path)) {
      return servletMap.get(path);
    }

    // 가장 긴 매칭 경로 찾기
    String bestMatch = "";
    for (String servletPath : servletMap.keySet()) {
      if (path.startsWith(servletPath) && servletPath.length() > bestMatch.length()) {
        bestMatch = servletPath;
      }
    }

    // 루트 경로 검사 ("/")
    if (bestMatch.isEmpty() && servletMap.containsKey("/")) {
      return servletMap.get("/");
    }

    // 빈 문자열 경로 검사 (루트 매핑)
    if (bestMatch.isEmpty() && servletMap.containsKey("")) {
      return servletMap.get("");
    }

    return bestMatch.isEmpty() ? null : servletMap.get(bestMatch);
  }


  private void closeServerSocket() {
    if (serverSocket != null && !serverSocket.isClosed()) {
      try {
        serverSocket.close();
      } catch (IOException e) {
        System.err.println("서버 소켓 닫기 오류: " + e.getMessage());
      }
    }
  }

  public boolean isRunning() {
    return running.get();
  }
}