package org.depth.transaction;

public interface TransactionManager {
    /**
     * 트랜잭션을 시작합니다.
     */
    void begin();

    /**
     * 트랜잭션을 커밋합니다.
     */
    void commit();

    /**
     * 트랜잭션을 롤백합니다.
     */
    void rollback();

    /**
     * 현재 활성 트랜잭션이 있는지 확인합니다.
     * @return 활성 트랜잭션이 있으면 true
     */
     boolean isActive();
}