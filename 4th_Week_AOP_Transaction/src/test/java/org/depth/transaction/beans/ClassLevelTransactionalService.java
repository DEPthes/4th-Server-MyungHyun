package org.depth.transaction.beans;

import org.depth.transaction.Transactional;

@Transactional
public class ClassLevelTransactionalService {
    public static final String CLASS_SUCCESS_MSG = "ClassLevelTransactionalService: SUCCESS";
    public static final String CLASS_FAILURE_MSG_PART = "ClassLevelTransactionalService: FAIL";

    public String publicSuccessMethod() { // Should be transactional
        System.out.println("ClassLevelTransactionalService.publicSuccessMethod() executed");
        return CLASS_SUCCESS_MSG;
    }

    public String publicFailureMethod() { // Should be transactional
        System.out.println("ClassLevelTransactionalService.publicFailureMethod() executed, will throw exception");
        throw new RuntimeException(CLASS_FAILURE_MSG_PART + " - Expected test exception");
    }

    protected String protectedMethod() { // Should not be transactional by default based on AnnotationMethodMatcher
        System.out.println("ClassLevelTransactionalService.protectedMethod() executed");
        return "PROTECTED_OK";
    }
}