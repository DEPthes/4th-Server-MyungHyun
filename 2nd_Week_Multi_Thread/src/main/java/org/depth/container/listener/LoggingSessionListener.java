package org.depth.container.listener;

import org.depth.http.model.HttpSession;

/**
 * 세션 이벤트를 로깅하는 세션 리스너 구현체
 */
public class LoggingSessionListener implements SessionListener {
    
    @Override
    public void sessionCreated(HttpSession session) {
        System.out.println("[세션 생성] ID: " + session.getId() + ", 시간: " + System.currentTimeMillis());
    }
    
    @Override
    public void sessionAccessed(HttpSession session) {
        System.out.println("[세션 접근] ID: " + session.getId() + ", 시간: " + System.currentTimeMillis());
    }
    
    @Override
    public void sessionInvalidated(HttpSession session) {
        System.out.println("[세션 무효화] ID: " + session.getId() + ", 시간: " + System.currentTimeMillis());
    }
    
    @Override
    public void attributeAdded(HttpSession session, String name, Object value) {
        System.out.println("[세션 속성 추가] ID: " + session.getId() + ", 속성: " + name + ", 값: " + value);
    }
    
    @Override
    public void attributeRemoved(HttpSession session, String name) {
        System.out.println("[세션 속성 제거] ID: " + session.getId() + ", 속성: " + name);
    }
} 