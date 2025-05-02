package org.depth.container;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.depth.container.filter.Filter;
import org.depth.container.filter.FilterChain;
import org.depth.container.request.HttpRequestHandler;
import org.depth.servlet.http.HttpServlet;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class HttpServletContainer implements Runnable {
    private final PathRoutingServletMap servletMap;
    private final HttpRequestHandler httpRequestHandler;
    private final ExecutorService executorService;
    @Getter
    private final List<Filter> filters = new ArrayList<>();
    private int port;
    private volatile boolean isRunning = false;
    private ServerSocket serverSocket;

    public void registerServlet(HttpServlet servlet) {
        servlet.init();
        this.servletMap.put(servlet.getServletPath(), servlet);
    }

    public void unregisterServlet(HttpServlet servlet) {
        servlet.destroy();
        this.servletMap.remove(servlet.getServletPath());
    }

    public void registerFilter(Filter filter) {
        filter.init();
        this.filters.add(filter);
    }

    public void unregisterFilter(Filter filter) {
        filter.destroy();
        this.filters.remove(filter);
    }

    public HttpServletContainer() {
        this.servletMap = new PathRoutingServletMap();
        this.httpRequestHandler = new HttpRequestHandler(this.servletMap, this.filters);
        this.executorService = java.util.concurrent.Executors.newFixedThreadPool(10);
    }

    public HttpServletContainer(ExecutorService executorService) {
        this.servletMap = new PathRoutingServletMap();
        this.httpRequestHandler = new HttpRequestHandler(this.servletMap, this.filters);
        this.executorService = executorService;
    }

    @Override
    public void run() {
        try {
            System.out.println("Server started on port " + this.port);
            isRunning = true;
            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                executorService.submit(() -> httpRequestHandler.handle(clientSocket));
            }
        } catch (IOException e) {
            if (isRunning) {
                 e.printStackTrace();
            }
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("Server stopped on port " + this.port);
        }
    }

    public void start(int port) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(this.port);
        new Thread(this).start();
    }

    public void stop() {
        isRunning = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
