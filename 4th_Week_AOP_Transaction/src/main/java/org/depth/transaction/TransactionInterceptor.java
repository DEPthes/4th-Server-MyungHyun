package org.depth.transaction;

import org.depth.aop.AroundAdvice;
import org.depth.aop.invocation.MethodInvocation;

public class TransactionInterceptor implements AroundAdvice {
    private final TransactionManager transactionManager;

    public TransactionInterceptor(TransactionManager transactionManager) {
        if (transactionManager == null) {
            throw new IllegalArgumentException("TransactionManager must not be null");
        }
        this.transactionManager = transactionManager;
    }


    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        boolean existingTransaction = transactionManager.isActive();


        Object result;
        try {
            if (!existingTransaction) { //  PROPAGATION_REQUIRED 와 유사한 동작
                this.transactionManager.begin();
            }

            result = invocation.proceed(); // 실제 타겟 메서드 호출

            if (!existingTransaction) {
                this.transactionManager.commit();
            }
        } catch (Throwable ex) {
            if (!existingTransaction) {
                try {
                    this.transactionManager.rollback();
                } catch (Exception rbEx) {
                    System.err.println("ERROR (Thread: " + Thread.currentThread().getName() + "): Transaction rollback failed: " + rbEx.getMessage());
                    // 롤백 실패를 로깅하지만, 원래의 예외(ex)는 계속 전파되어야 합니다.
                }
            }
            throw ex; // 원래의 예외를 다시 던짐
        }
        return result;
    }
}
