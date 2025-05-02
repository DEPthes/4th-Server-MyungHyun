package org.depth.http.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HttpSession {
    @Getter
    private final String id;
    
    @Getter
    @Setter
    private Instant expireTime;
    
    private final Map<String, Object> attributes = new HashMap<>();
    
    // 기본 세션 유효 시간: 30분
    private static final long DEFAULT_MAX_INACTIVE_INTERVAL = 30 * 60;
    
    public HttpSession() {
        this.id = UUID.randomUUID().toString();
        this.resetExpireTime();
    }
    
    public HttpSession(String id) {
        this.id = id;
        this.resetExpireTime();
    }
    
    /**
     * 세션 만료 시간을 현재 시간으로부터 기본 유효 시간(30분) 후로 재설정합니다.
     */
    public void resetExpireTime() {
        this.expireTime = Instant.now().plusSeconds(DEFAULT_MAX_INACTIVE_INTERVAL);
    }
    
    /**
     * 현재 시간 기준으로 세션이 만료되었는지 확인합니다.
     * @return 만료 여부
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expireTime);
    }
    
    /**
     * 세션에 속성을 설정합니다.
     * @param name 속성 이름
     * @param value 속성 값
     */
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }
    
    /**
     * 세션에서 속성을 조회합니다.
     * @param name 속성 이름
     * @return 속성 값 (없으면 null)
     */
    public Object getAttribute(String name) {
        return attributes.get(name);
    }
    
    /**
     * 세션에서 속성을 제거합니다.
     * @param name 속성 이름
     */
    public void removeAttribute(String name) {
        attributes.remove(name);
    }
    
    /**
     * 세션의 모든 속성을 반환합니다.
     * @return 속성 맵
     */
    public Map<String, Object> getAttributes() {
        return new HashMap<>(attributes);
    }
    
    /**
     * 세션의 모든 속성을 초기화합니다.
     */
    public void invalidate() {
        attributes.clear();
    }
} 