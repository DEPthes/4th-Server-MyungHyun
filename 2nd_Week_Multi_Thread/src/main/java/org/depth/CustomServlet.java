package org.depth;

import org.depth.servlet.http.HttpServlet;
import org.depth.servlet.http.HttpServletRequest;
import org.depth.servlet.http.HttpServletResponse;

public class CustomServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) {
        if(request.getMethod().equals("GET")) {
            StringBuilder builder = new StringBuilder();
            builder.append("<html><body>");
            builder.append("<h1>Custom Servlet</h1>");
            builder.append("<p>Request Method: ").append(request.getMethod()).append("</p>");
            builder.append("<p>Request Path: ").append(request.getPath()).append("</p>");
            builder.append("<p>Request Version: ").append(request.getVersion()).append("</p>");
            builder.append("<p>Request Headers: </p><ul>");
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
