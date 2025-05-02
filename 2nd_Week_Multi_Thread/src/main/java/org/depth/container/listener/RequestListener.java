package org.depth.container.listener;

import org.depth.servlet.http.HttpServletRequest;
import org.depth.servlet.http.HttpServletResponse;

/**
 * HTTP 요청 처리 이벤트를 수신하는 리스너 인터페이스
 */
public interface RequestListener {
    
    /**
     * 요청이 수신되어 처리되기 전에 호출됩니다.
     * 
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @return 요청 처리를 계속해야 하면 true, 중단해야 하면 false
     */
    boolean requestInitialized(HttpServletRequest request, HttpServletResponse response);
    
    /**
     * 요청이 처리된 후 호출됩니다.
     * 
     * @param request HTTP 요청
     * @param response HTTP 응답
     */
    void requestCompleted(HttpServletRequest request, HttpServletResponse response);
    
    /**
     * 요청 처리 중 예외가 발생했을 때 호출됩니다.
     * 
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param exception 발생한 예외
     * @return 예외를 처리했으면 true, 다른 처리기에 전달해야 하면 false
     */
    boolean requestError(HttpServletRequest request, HttpServletResponse response, Throwable exception);
    
    /**
     * 요청 속성이 추가되거나 변경될 때 호출됩니다.
     * 
     * @param request HTTP 요청
     * @param name 속성 이름
     * @param value 속성 값
     */
    void attributeAdded(HttpServletRequest request, String name, Object value);
    
    /**
     * 요청 속성이 제거될 때 호출됩니다.
     * 
     * @param request HTTP 요청
     * @param name 속성 이름
     */
    void attributeRemoved(HttpServletRequest request, String name);
} 