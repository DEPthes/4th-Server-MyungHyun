package org.depth.http.model;

import lombok.Getter;
import lombok.Setter;
import org.depth.container.listener.SessionListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HttpSession {
    @Getter
    private final String id;
    
    @Getter
    @Setter
    private Instant expireTime;
    
    private final Map<String, Object> attributes = new HashMap<>();
    
    private List<SessionListener> listeners = new ArrayList<>();
    
    private static final long DEFAULT_MAX_INACTIVE_INTERVAL = 30 * 60;
    
    public HttpSession() {
        this.id = UUID.randomUUID().toString();
        this.resetExpireTime();
    }
    
    public HttpSession(String id) {
        this.id = id;
        this.resetExpireTime();
    }
    
    // 세션 만료 시간 리셋
    public void resetExpireTime() {
        this.expireTime = Instant.now().plusSeconds(DEFAULT_MAX_INACTIVE_INTERVAL);
    }
    
    // 세션 만료 여부 확인
    public boolean isExpired() {
        return Instant.now().isAfter(expireTime);
    }
    
    // 세션 속성 설정
    public void setAttribute(String name, Object value) {
        Object oldValue = attributes.put(name, value);
        
        if (oldValue != value) {
            notifyAttributeAdded(name, value);
        }
    }
    
    // 세션 속성 조회
    public Object getAttribute(String name) {
        return attributes.get(name);
    }
    
    // 세션 속성 제거
    public void removeAttribute(String name) {
        if (attributes.containsKey(name)) {
            attributes.remove(name);
            notifyAttributeRemoved(name);
        }
    }
    
    // 모든 세션 속성 반환
    public Map<String, Object> getAttributes() {
        return new HashMap<>(attributes);
    }
    
    // 세션 무효화
    public void invalidate() {
        attributes.clear();
        notifySessionInvalidated();
    }
    
    // 세션 리스너 추가
    public void addSessionListener(SessionListener listener) {
        listeners.add(listener);
    }
    
    // 세션 리스너 제거
    public void removeSessionListener(SessionListener listener) {
        listeners.remove(listener);
    }
    
    // 세션 접근 알림
    public void notifySessionAccessed() {
        for (SessionListener listener : listeners) {
            listener.sessionAccessed(this);
        }
    }
    
    // 세션 생성 알림
    public void notifySessionCreated() {
        for (SessionListener listener : listeners) {
            listener.sessionCreated(this);
        }
    }
    
    // 세션 무효화 알림
    private void notifySessionInvalidated() {
        for (SessionListener listener : listeners) {
            listener.sessionInvalidated(this);
        }
    }
    
    // 속성 추가 알림
    private void notifyAttributeAdded(String name, Object value) {
        for (SessionListener listener : listeners) {
            listener.attributeAdded(this, name, value);
        }
    }
    
    // 속성 제거 알림
    private void notifyAttributeRemoved(String name) {
        for (SessionListener listener : listeners) {
            listener.attributeRemoved(this, name);
        }
    }
} 