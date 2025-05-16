package org.depth.transaction.support;

import org.depth.transaction.TransactionManager;

public class DummyTransactionManager implements TransactionManager {
    private static final ThreadLocal<Boolean> activeTransactionInThread = ThreadLocal.withInitial(() -> false);

    @Override
    public void begin() {
        if (activeTransactionInThread.get()) {
            // 간단한 예시로, 중첩 트랜잭션은 아직 지원하지 않는다고 가정하거나 로깅만 합니다.
            System.out.println("WARN (Thread: " + Thread.currentThread().getName() + "): Transaction is already active.");
            // 실제 구현에서는 PROPAGATION_NESTED 등을 처리해야 합니다.
            return; // 또는 예외 발생
        }
        System.out.println("INFO (Thread: " + Thread.currentThread().getName() + "): [DummyTransactionManager] Beginning transaction.");
        activeTransactionInThread.set(true);
    }

    @Override
    public void commit() {
        if (!activeTransactionInThread.get()) {
            System.out.println("WARN (Thread: " + Thread.currentThread().getName() + "): No active transaction to commit.");
            return;
        }
        System.out.println("INFO (Thread: " + Thread.currentThread().getName() + "): [DummyTransactionManager] Committing transaction.");
        activeTransactionInThread.set(false);
    }

    @Override
    public void rollback() {
        if (!activeTransactionInThread.get()) {
            System.out.println("WARN (Thread: " + Thread.currentThread().getName() + "): No active transaction to rollback.");
            return;
        }
        System.out.println("INFO (Thread: " + Thread.currentThread().getName() + "): [DummyTransactionManager] Rolling back transaction.");
        activeTransactionInThread.set(false);
    }

     public boolean isActive() { // TransactionManager 인터페이스에 isActive가 있다면 구현
         return activeTransactionInThread.get();
     }
}