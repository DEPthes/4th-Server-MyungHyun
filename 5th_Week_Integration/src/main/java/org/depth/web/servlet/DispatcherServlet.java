package org.depth.web.servlet;


import com.google.gson.Gson;
import lombok.SneakyThrows;
import org.depth.web.servlet.http.HttpServlet;
import org.depth.web.servlet.http.HttpServletRequest;
import org.depth.web.servlet.http.HttpServletResponse;

public class DispatcherServlet extends HttpServlet {
    private final RequestHandlerMapping requestHandlerMapping = new RequestHandlerMapping();

    @Override
    @SneakyThrows
    protected void service(HttpServletRequest request, HttpServletResponse response) {
        // 요청 URL에 따라 적절한 핸들러를 찾고 실행
        RequestHandlerMapping.RequestHandlerMappingItem found = requestHandlerMapping.getHandler(request.getPath());
        Object invokeResponse = found.getMethodToCall().invoke(found.getController());//TODO: Request, Response 객체 전달

        Gson gson = new Gson();
        String jsonResponse = gson.toJson(invokeResponse);
        response.addHeader("Content-Type", "application/json");
        response.setBody(jsonResponse);
        response.setStatusCode(200);
    }

    @Override
    public String getServletName() {
        return "DispatcherServlet";
    }

    @Override
    public String getServletPath() {
        return "/";
    }
}
