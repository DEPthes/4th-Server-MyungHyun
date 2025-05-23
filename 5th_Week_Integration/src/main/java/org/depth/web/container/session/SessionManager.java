package org.depth.web.container.session;

import org.depth.web.container.listener.SessionListener;
import org.depth.web.http.model.HttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SessionManager {
    private final Map<String, HttpSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final List<SessionListener> globalSessionListeners = new ArrayList<>();
    
    // 세션 클린업 주기 (기본 10분)
    private static final long CLEANUP_INTERVAL = 10;
    
    // 세션 관리자 생성자
    public SessionManager() {
        // 주기적으로 만료된 세션을 정리하는 작업 시작
        startSessionCleanup();
    }
    
    // 전역 세션 리스너 추가
    public void addSessionListener(SessionListener listener) {
        globalSessionListeners.add(listener);
    }
    
    // 전역 세션 리스너 제거
    public void removeSessionListener(SessionListener listener) {
        globalSessionListeners.remove(listener);
    }
    
    // 세션 ID로 세션 조회
    public HttpSession getSession(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        
        HttpSession session = sessions.get(sessionId);
        
        if (session != null) {
            if (session.isExpired()) {
                invalidateSession(sessionId); // 만료된 세션 정리
                return null;
            }
            // 세션 접근 시 만료 시간 갱신
            session.resetExpireTime();
            
            // 세션 접근 이벤트 알림
            session.notifySessionAccessed();
        }
        
        return session;
    }
    
    // 새 세션 생성
    public HttpSession createSession() {
        HttpSession session = new HttpSession();
        
        // 전역 세션 리스너 등록
        for (SessionListener listener : globalSessionListeners) {
            session.addSessionListener(listener);
        }
        
        sessions.put(session.getId(), session);
        
        // 세션 생성 이벤트 알림
        session.notifySessionCreated();
        
        return session;
    }
    
    // 세션 무효화
    public void invalidateSession(String sessionId) {
        HttpSession session = sessions.remove(sessionId);
        if (session != null) {
            session.invalidate();
        }
    }
    
    // 만료된 세션 정리 작업 시작
    private void startSessionCleanup() {
        scheduler.scheduleAtFixedRate(() -> {
            sessions.entrySet().removeIf(entry -> {
                boolean expired = entry.getValue().isExpired();
                if (expired) {
                    entry.getValue().invalidate(); // 세션 무효화 이벤트 발생
                }
                return expired;
            });
        }, CLEANUP_INTERVAL, CLEANUP_INTERVAL, TimeUnit.MINUTES);
    }
    
    // 세션 관리자 종료
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // 모든 세션 무효화
        for (HttpSession session : sessions.values()) {
            session.invalidate();
        }
        
        sessions.clear();
    }
} 