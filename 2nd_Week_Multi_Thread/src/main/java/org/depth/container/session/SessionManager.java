package org.depth.container.session;

import org.depth.http.model.HttpSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SessionManager {
    private final Map<String, HttpSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    // 세션 클린업 주기 (기본 10분)
    private static final long CLEANUP_INTERVAL = 10;
    
    public SessionManager() {
        // 주기적으로 만료된 세션을 정리하는 작업 시작
        startSessionCleanup();
    }
    
    /**
     * 세션 ID에 해당하는 세션을 가져옵니다. 만료된 세션은 null을 반환합니다.
     * @param sessionId 세션 ID
     * @return 세션 객체 (없거나 만료된 경우 null)
     */
    public HttpSession getSession(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        
        HttpSession session = sessions.get(sessionId);
        
        if (session != null) {
            if (session.isExpired()) {
                sessions.remove(sessionId);
                return null;
            }
            // 세션 접근 시 만료 시간 갱신
            session.resetExpireTime();
        }
        
        return session;
    }
    
    /**
     * 새 세션을 생성합니다.
     * @return 새로 생성된 세션
     */
    public HttpSession createSession() {
        HttpSession session = new HttpSession();
        sessions.put(session.getId(), session);
        return session;
    }
    
    /**
     * 특정 세션을 무효화합니다.
     * @param sessionId 세션 ID
     */
    public void invalidateSession(String sessionId) {
        HttpSession session = sessions.remove(sessionId);
        if (session != null) {
            session.invalidate();
        }
    }
    
    /**
     * 주기적으로 만료된 세션을 정리하는 작업을 시작합니다.
     */
    private void startSessionCleanup() {
        scheduler.scheduleAtFixedRate(() -> {
            sessions.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }, CLEANUP_INTERVAL, CLEANUP_INTERVAL, TimeUnit.MINUTES);
    }
    
    /**
     * 세션 관리자를 종료합니다.
     */
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
        sessions.clear();
    }
} 