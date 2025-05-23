package org.depth.web.container.listener;

import org.depth.web.servlet.http.HttpServletRequest;
import org.depth.web.servlet.http.HttpServletResponse;

/**
 * HTTP 요청 처리 이벤트를 수신하는 리스너 인터페이스
 */
public interface RequestListener {
    
    // 요청 초기화 시 호출
    boolean requestInitialized(HttpServletRequest request, HttpServletResponse response);
    
    // 요청 완료 시 호출
    void requestCompleted(HttpServletRequest request, HttpServletResponse response);
    
    // 요청 오류 시 호출
    boolean requestError(HttpServletRequest request, HttpServletResponse response, Throwable exception);
    
    // 요청 속성 추가 시 호출
    void attributeAdded(HttpServletRequest request, String name, Object value);
    
    // 요청 속성 제거 시 호출
    void attributeRemoved(HttpServletRequest request, String name);
} 