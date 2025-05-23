package org.depth.web.servlet;

import com.google.gson.Gson;
import lombok.Setter;
import lombok.SneakyThrows;
import org.depth.beans.BeanDefinition;
import org.depth.beans.factory.ListableBeanFactory;
import org.depth.beans.factory.context.ApplicationContext;
import org.depth.beans.factory.exception.BeansException;
import org.depth.web.annotation.Controller; // @Controller 어노테이션
import org.depth.web.annotation.RequestMapping;
import org.depth.web.context.WebApplicationContext;
import org.depth.web.http.handler.HttpResponseWriter;
import org.depth.web.servlet.http.HttpServlet;
import org.depth.web.servlet.http.HttpServletRequest;
import org.depth.web.servlet.http.HttpServletResponse;

import java.lang.reflect.Method;

public class DispatcherServlet extends HttpServlet {
    private final RequestHandlerMapping requestHandlerMapping = new RequestHandlerMapping();

    @Setter
    private WebApplicationContext webApplicationContext;

    @Override
    public void init() {
        super.init();
        if (this.webApplicationContext == null) {
            System.err.println("DispatcherServlet WARN: WebApplicationContext is not set. Handler mappings will not be initialized.");
            return;
        }
        initHandlerMappings(this.webApplicationContext);
    }

    /**
     * ApplicationContext에서 @Controller 어노테이션이 붙은 빈들을 스캔하여,
     * 내부의 @RequestMapping 어노테이션을 가진 메서드를 찾아 RequestHandlerMapping에 등록합니다.
     */
    protected void initHandlerMappings(ApplicationContext context) {
        if (!(context instanceof ListableBeanFactory)) {
            System.err.println("DispatcherServlet WARN: ApplicationContext is not a ListableBeanFactory. Cannot scan for handlers.");
            return;
        }

        ListableBeanFactory beanFactory = (ListableBeanFactory) context;
        String[] beanNames = beanFactory.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
            Class<?> originalBeanClass = bd.getBeanClass();

            if (originalBeanClass.isAnnotationPresent(Controller.class)) {
                Object controllerBean;
                try {
                    controllerBean = beanFactory.getBean(beanName);
                } catch (BeansException e) {
                    System.err.println("DispatcherServlet WARN: Error creating bean '" + beanName + "' for handler mapping. Skipping. Error: " + e.getMessage());
                    continue;
                }

                Method[] methods = originalBeanClass.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping mappingAnnotation = method.getAnnotation(RequestMapping.class);
                        String path = mappingAnnotation.value();
                        String httpMethod = mappingAnnotation.method();

                        method.setAccessible(true);

                        this.requestHandlerMapping.addHandler(path, httpMethod, controllerBean, method);
                        System.out.println("DispatcherServlet INFO: Mapped HTTP " + httpMethod + " [" + path + "] onto " + originalBeanClass.getName() + "." + method.getName());
                    }
                }
            }
        }
    }

    @Override
    @SneakyThrows // Lombok 어노테이션으로 checked exception을 uncheck으로 변환 (실제로는 try-catch 권장)
    protected void service(HttpServletRequest request, HttpServletResponse response) {
        RequestHandlerMapping.RequestHandlerMappingItem foundHandler = null;
        try {
            foundHandler = requestHandlerMapping.matchByPath(request.getPath(), request.getMethod());
        } catch (IllegalArgumentException e) {
            // matchByPath에서 적절한 핸들러를 찾지 못하면 예외 발생 (405 Method Not Allowed 또는 404 Not Found)
            // RequestHandlerMapping.matchByPath의 구현에 따라 상태 코드 결정
            if (e.getMessage() != null && e.getMessage().startsWith("Method not allowed")) {
                response.setStatusCode(405);
                response.setStatusText("Method Not Allowed");
                response.setBody("<html><body><h1>405 Method Not Allowed</h1><p>" + e.getMessage() + "</p></body></html>");
            } else {
                response.setStatusCode(404);
                response.setStatusText("Not Found");
                response.setBody("<html><body><h1>404 Not Found</h1><p>No handler found for " + request.getMethod() + " " + request.getPath() + "</p></body></html>");
            }
            HttpResponseWriter.standardizeResponse(response); // 응답 표준화
            return;
        }

        // 위 catch 블록에서 처리되므로, foundHandler가 null인 경우는 거의 없음. (방어적 코드)
        if (foundHandler == null) {
            response.setStatusCode(404);
            response.setStatusText("Not Found");
            response.setBody("<html><body><h1>404 Not Found - Handler not resolved</h1></body></html>");
            HttpResponseWriter.standardizeResponse(response);
            return;
        }

        Object controller = foundHandler.getController();
        Method methodToCall = foundHandler.getMethodToCall();
        Object invokeResponse;

        try {
            // TODO: 실제 프로덕션 코드에서는 HandlerAdapter를 통해 다양한 메서드 시그니처를 유연하게 지원해야 합니다.
            if (methodToCall.getParameterCount() == 2 &&
                    methodToCall.getParameterTypes()[0].isAssignableFrom(HttpServletRequest.class) &&
                    methodToCall.getParameterTypes()[1].isAssignableFrom(HttpServletResponse.class)) {
                invokeResponse = methodToCall.invoke(controller, request, response);
            } else if (methodToCall.getParameterCount() == 0) {
                invokeResponse = methodToCall.invoke(controller);
            } else {
                // 지원하지 않는 메서드 시그니처에 대한 기본 처리
                System.err.println("DispatcherServlet ERROR: Unsupported method signature for handler: " + methodToCall.getName());
                throw new NoSuchMethodException("Suitable handler method signature not found in " + methodToCall.getName() +
                        " for path " + request.getPath() + ". Parameters found: " + methodToCall.getParameterCount());
            }

            // 핸들러 메서드가 직접 응답을 작성하지 않고 값을 반환한 경우 (void가 아닌 경우)
            // 그리고 응답이 아직 커밋되지 않았다고 가정 (isCommitted() 기능이 없으므로)
            if (invokeResponse != null && methodToCall.getReturnType() != void.class) {
                Gson gson = new Gson();
                String jsonResponse = gson.toJson(invokeResponse);
                response.addHeader("Content-Type", "application/json;charset=UTF-8");
                response.setBody(jsonResponse);
                // 핸들러에서 상태 코드를 변경하지 않았다면 기본 성공(200)으로 설정
                if (response.getStatusCode() == 0 || response.getStatusCode() == 405) { // 초기값 또는 HttpServlet의 기본값
                    response.setStatusCode(200);
                    response.setStatusText("OK");
                }
            }
            // else: 핸들러 메서드가 void 타입이거나 null을 반환한 경우,
            //       또는 메서드 내에서 직접 response 객체를 통해 응답을 작성한 것으로 간주.
            //       (예: response.getWriter().print(...), response.setStatusCode(...))

        } catch (Exception e) {
            System.err.println("DispatcherServlet ERROR: Handler execution failed for " + request.getPath() + ": " + e.getMessage());
            // 예외의 원인도 로깅하면 디버깅에 도움
            if (e.getCause() != null) {
                System.err.println("Caused by: " + e.getCause().getMessage());
            }
            e.printStackTrace(); // 개발 환경에서는 스택 트레이스 출력

            // isCommitted() 기능이 없으므로, 일단 응답을 덮어쓴다고 가정.
            response.setStatusCode(500);
            response.setStatusText("Internal Server Error");
            response.setBody("<html><body><h1>500 Internal Server Error</h1><p>Error processing request. Please check server logs.</p></body></html>");
        } finally {
            // isCommitted() 기능이 없으므로, 항상 응답 표준화 시도.
            // 핸들러가 이미 응답을 일부 작성/수정했다면, 이 메서드가 헤더 등을 변경할 수 있음에 유의.
            HttpResponseWriter.standardizeResponse(response);
        }
    }

    @Override
    public String getServletName() {
        return "DispatcherServlet";
    }

    @Override
    public String getServletPath() {
        // 이 서블릿이 처리할 기본 경로. 실제 컨테이너 설정에 따라 달라질 수 있습니다.
        return "/";
    }
}