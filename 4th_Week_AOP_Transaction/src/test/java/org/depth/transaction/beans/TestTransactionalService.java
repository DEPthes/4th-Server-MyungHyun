package org.depth.transaction.beans;

import org.depth.transaction.Transactional;

public class TestTransactionalService {
    public static final String SUCCESS_MSG = "TestTransactionalService: SUCCESS";
    public static final String FAILURE_MSG_PART = "TestTransactionalService: FAIL";

    @Transactional
    public String doSomethingSuccess() {
        System.out.println("TestTransactionalService.doSomethingSuccess() executed");
        return SUCCESS_MSG;
    }

    @Transactional
    public String doSomethingFailure() {
        System.out.println("TestTransactionalService.doSomethingFailure() executed, will throw exception");
        throw new RuntimeException(FAILURE_MSG_PART + " - Expected test exception");
    }

    public String doSomethingNonTransactional() {
        System.out.println("TestTransactionalService.doSomethingNonTransactional() executed");
        return "NON_TRANSACTIONAL_OK";
    }
}
