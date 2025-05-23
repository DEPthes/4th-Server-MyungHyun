package org.depth.web.servlet;

import com.google.gson.Gson;
import lombok.Setter;
import lombok.SneakyThrows;
import org.depth.beans.BeanDefinition;
import org.depth.beans.factory.exception.BeansException;
import org.depth.web.annotation.Controller;
import org.depth.web.annotation.RequestMapping;
import org.depth.web.context.GenericWebApplicationContext;
import org.depth.web.http.handler.HttpResponseWriter;
import org.depth.web.servlet.http.HttpServlet;
import org.depth.web.servlet.http.HttpServletRequest;
import org.depth.web.servlet.http.HttpServletResponse;

import java.lang.reflect.Method;

public class DispatcherServlet extends HttpServlet {
    private final RequestHandlerMapping requestHandlerMapping = new RequestHandlerMapping();

    @Setter
    private GenericWebApplicationContext webApplicationContext;

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
     * 여기서 context는 GenericApplicationContext의 인스턴스라고 가정합니다.
     */
    protected void initHandlerMappings(GenericWebApplicationContext context) {
        String[] beanNames = context.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            BeanDefinition bd = context.getBeanDefinition(beanName);
            if (bd == null) { // 방어적 코드: BeanDefinition이 null인 경우 건너뛰기
                System.err.println("DispatcherServlet WARN: BeanDefinition not found for bean name: " + beanName + ". Skipping.");
                continue;
            }
            Class<?> originalBeanClass = bd.getBeanClass();
            if (originalBeanClass == null) { // 방어적 코드: Bean 클래스가 null인 경우 건너뛰기
                System.err.println("DispatcherServlet WARN: Bean class not found in BeanDefinition for bean name: " + beanName + ". Skipping.");
                continue;
            }


            if (originalBeanClass.isAnnotationPresent(Controller.class)) {
                Object controllerBean;
                try {
                    // getBean은 ApplicationContext (즉, BeanFactory) 인터페이스의 메서드입니다.
                    controllerBean = context.getBean(beanName);
                } catch (BeansException e) {
                    System.err.println("DispatcherServlet WARN: Error creating bean '" + beanName + "' for handler mapping. Skipping. Error: " + e.getMessage());
                    continue;
                }

                // controllerBean이 null인 경우에 대한 방어 코드 (getBean이 예외를 던지지 않고 null을 반환할 수 있다면)
                if (controllerBean == null) {
                    System.err.println("DispatcherServlet WARN: Bean instance for '" + beanName + "' is null. Skipping handler mapping.");
                    continue;
                }

                // AOP 프록시를 고려한다면 controllerBean.getClass()를 사용할 수 있지만,
                // 어노테이션은 주로 원본 클래스에 정의되므로 originalBeanClass를 사용하는 것이 일반적입니다.
                // 현재 AopProxyBeanPostProcessor는 CGLIB를 사용하므로 originalBeanClass로도 프록시된 메서드에 접근 가능합니다.
                Method[] methods = originalBeanClass.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping mappingAnnotation = method.getAnnotation(RequestMapping.class);
                        String path = mappingAnnotation.value();
                        String httpMethod = mappingAnnotation.method();

                        method.setAccessible(true); // private 메서드 접근 허용

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
            if (e.getMessage() != null && e.getMessage().startsWith("Method not allowed")) {
                response.setStatusCode(405);
                response.setStatusText("Method Not Allowed");
                response.setBody("<html><body><h1>405 Method Not Allowed</h1><p>" + e.getMessage() + "</p></body></html>");
            } else {
                response.setStatusCode(404);
                response.setStatusText("Not Found");
                response.setBody("<html><body><h1>404 Not Found</h1><p>No handler found for " + request.getMethod() + " " + request.getPath() + "</p></body></html>");
            }
            HttpResponseWriter.standardizeResponse(response);
            return;
        }

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
            if (methodToCall.getParameterCount() == 2 &&
                    methodToCall.getParameterTypes()[0].isAssignableFrom(HttpServletRequest.class) &&
                    methodToCall.getParameterTypes()[1].isAssignableFrom(HttpServletResponse.class)) {
                invokeResponse = methodToCall.invoke(controller, request, response);
            } else if (methodToCall.getParameterCount() == 0) {
                invokeResponse = methodToCall.invoke(controller);
            } else {
                System.err.println("DispatcherServlet ERROR: Unsupported method signature for handler: " + methodToCall.getName());
                throw new NoSuchMethodException("Suitable handler method signature not found in " + methodToCall.getName() +
                        " for path " + request.getPath() + ". Parameters found: " + methodToCall.getParameterCount());
            }

            if (invokeResponse != null && methodToCall.getReturnType() != void.class) {
                Gson gson = new Gson();
                String jsonResponse = gson.toJson(invokeResponse);
                response.addHeader("Content-Type", "application/json;charset=UTF-8");
                response.setBody(jsonResponse);
                if (response.getStatusCode() == 0 || response.getStatusCode() == 405) {
                    response.setStatusCode(200);
                    response.setStatusText("OK");
                }
            }

        } catch (Exception e) {
            System.err.println("DispatcherServlet ERROR: Handler execution failed for " + request.getPath() + ": " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Caused by: " + e.getCause().getMessage());
            }
            e.printStackTrace();

            response.setStatusCode(500);
            response.setStatusText("Internal Server Error");
            response.setBody("<html><body><h1>500 Internal Server Error</h1><p>Error processing request. Please check server logs.</p></body></html>");
        } finally {
            HttpResponseWriter.standardizeResponse(response);
        }
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