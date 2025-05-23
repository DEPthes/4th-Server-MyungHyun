package org.depth.web.container.listener;


import org.depth.web.http.model.HttpSession;

/**
 * HTTP 세션 생명주기 이벤트를 수신하는 리스너 인터페이스
 */
public interface SessionListener {
    
    // 세션 생성 시 호출
    void sessionCreated(HttpSession session);
    
    // 세션 접근 시 호출
    void sessionAccessed(HttpSession session);
    
    // 세션 무효화 시 호출
    void sessionInvalidated(HttpSession session);
    
    // 세션 속성 추가/변경 시 호출
    void attributeAdded(HttpSession session, String name, Object value);
    
    // 세션 속성 제거 시 호출
    void attributeRemoved(HttpSession session, String name);
} 