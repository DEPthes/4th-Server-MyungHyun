package org.depth;

import org.depth.http.model.HttpSession;
import org.depth.servlet.http.HttpServlet;
import org.depth.servlet.http.HttpServletRequest;
import org.depth.servlet.http.HttpServletResponse;

import java.time.Instant;

public class CustomServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) {
        if(request.getMethod().equals("GET")) {
            // 세션에서 방문 횟수 가져오기 및 증가
            HttpSession session = request.getSession();
            Integer visitCount = (Integer) session.getAttribute("visitCount");
            if (visitCount == null) {
                visitCount = 1;
            } else {
                visitCount++;
            }
            session.setAttribute("visitCount", visitCount);
            session.setAttribute("lastVisit", Instant.now().toString());
            
            StringBuilder builder = new StringBuilder();
            builder.append("<html><body>");
            builder.append("<h1>Custom Servlet</h1>");
            builder.append("<p>Request Method: ").append(request.getMethod()).append("</p>");
            builder.append("<p>Request Path: ").append(request.getPath()).append("</p>");
            builder.append("<p>Request Version: ").append(request.getVersion()).append("</p>");
            
            // 세션 정보 출력
            builder.append("<h2>Session Information</h2>");
            builder.append("<p>Session ID: ").append(session.getId()).append("</p>");
            builder.append("<p>Session Expire Time: ").append(session.getExpireTime()).append("</p>");
            builder.append("<p>Visit Count: ").append(visitCount).append("</p>");
            builder.append("<p>Last Visit: ").append(session.getAttribute("lastVisit")).append("</p>");
            
            builder.append("<h2>Request Headers</h2><ul>");
            for (var header : request.getHeaders()) {
                builder.append("<li>").append(header.getName()).append(": ").append(header.getValue()).append("</li>");
            }
            builder.append("</ul>");
            builder.append("<p>Request Body: ").append(request.getBody()).append("</p>");
            builder.append("</body></html>");

            response.setStatusCode(200);
            response.setStatusText("OK");
            response.addHeader("Content-Type", "text/html");
            response.setBody(builder.toString());
        } else {
            response.setStatusCode(405);
            response.setStatusText("Method Not Allowed");
            response.addHeader("Content-Type", "text/plain");
            response.setBody("Method Not Allowed");
        }
    }

    @Override
    public String getServletName() {
        return "CustomServlet";
    }

    @Override
    public String getServletPath() {
        return "";
    }
}
