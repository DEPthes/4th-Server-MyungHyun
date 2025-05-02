package org.depth.container.listener;

import org.depth.http.model.HttpSession;

/**
 * HTTP 세션 생명주기 이벤트를 수신하는 리스너 인터페이스
 */
public interface SessionListener {
    
    /**
     * 세션이 생성될 때 호출됩니다.
     * 
     * @param session 새로 생성된 세션
     */
    void sessionCreated(HttpSession session);
    
    /**
     * 세션이 활성화될 때 호출됩니다.
     * 클라이언트가 세션 ID를 가진 쿠키로 요청할 때마다 호출됩니다.
     * 
     * @param session 활성화된 세션
     */
    void sessionAccessed(HttpSession session);
    
    /**
     * 세션이 무효화될 때 호출됩니다.
     * 세션이 만료되거나 명시적으로 무효화될 때 호출됩니다.
     * 
     * @param session 무효화된 세션
     */
    void sessionInvalidated(HttpSession session);
    
    /**
     * 세션 속성이 추가되거나 변경될 때 호출됩니다.
     * 
     * @param session 세션
     * @param name 속성 이름
     * @param value 속성 값
     */
    void attributeAdded(HttpSession session, String name, Object value);
    
    /**
     * 세션 속성이 제거될 때 호출됩니다.
     * 
     * @param session 세션
     * @param name 속성 이름
     */
    void attributeRemoved(HttpSession session, String name);
} 