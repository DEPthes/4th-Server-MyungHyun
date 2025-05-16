package org.depth.transaction;

import net.sf.cglib.proxy.Factory;
import org.depth.aop.DefaultAdvisor;
import org.depth.aop.DefaultPointcut;
import org.depth.aop.matcher.AnnotationClassMatcher;
import org.depth.aop.matcher.AnnotationMethodMatcher;
import org.depth.aop.proxy.AopProxyBeanPostProcessor;
import org.depth.beans.BeanDefinition;
import org.depth.beans.factory.ListableBeanFactory;
import org.depth.transaction.beans.ClassLevelTransactionalService;
import org.depth.transaction.beans.TestTransactionalService;
import org.depth.transaction.support.DummyTransactionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionIntegrationTest {

    private ListableBeanFactory beanFactory;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    /**
     * 테스트에서 사용할 Pointcut을 정의하는 내부 정적 클래스입니다.
     * @Transactional 어노테이션을 기준으로 클래스와 메소드를 매칭합니다.
     */
    public static class MyTransactionalPointcut extends DefaultPointcut {
        public MyTransactionalPointcut() {
            super(new AnnotationClassMatcher(Transactional.class),
                    new AnnotationMethodMatcher(Transactional.class));
        }
    }

    @BeforeEach
    void setUp() {
        beanFactory = new ListableBeanFactory();
        System.setOut(new PrintStream(outContent)); // 콘솔 출력 캡처 시작

        // 1. AOP PostProcessor 등록
        AopProxyBeanPostProcessor aopProcessor = new AopProxyBeanPostProcessor(beanFactory);
        beanFactory.addBeanPostProcessor(aopProcessor);

        // 2. TransactionManager 빈 등록 (DummyTransactionManager 사용)
        BeanDefinition txManagerDef = new BeanDefinition("txManager", DummyTransactionManager.class);
        beanFactory.registerBeanDefinition("txManager", txManagerDef);

        // 3. TransactionInterceptor 빈 등록 (생성자를 통해 txManager 주입)
        BeanDefinition txInterceptorDef = new BeanDefinition("txInterceptor", TransactionInterceptor.class);
        txInterceptorDef.addConstructorArgBeanName("txManager");
        beanFactory.registerBeanDefinition("txInterceptor", txInterceptorDef);

        // 4. Pointcut 빈 등록 (@Transactional 어노테이션 타겟)
        BeanDefinition pointcutDef = new BeanDefinition("transactionalPointcut", MyTransactionalPointcut.class);
        beanFactory.registerBeanDefinition("transactionalPointcut", pointcutDef);

        // 5. Advisor 빈 등록 (DefaultAdvisor가 Pointcut과 Interceptor를 연결)
        BeanDefinition advisorDef = new BeanDefinition("transactionAdvisor", DefaultAdvisor.class);
        // DefaultAdvisor 생성자: Pointcut pointcut, AroundAdvice advice
        advisorDef.addConstructorArgBeanName("transactionalPointcut"); // 첫 번째 생성자 인자로 Pointcut 빈 이름 제공
        advisorDef.addConstructorArgBeanName("txInterceptor");       // 두 번째 생성자 인자로 Advice 빈 이름 제공
        beanFactory.registerBeanDefinition("transactionAdvisor", advisorDef);

        // 6. 테스트용 서비스 빈들 등록
        BeanDefinition testServiceDef = new BeanDefinition("testTransactionalService", TestTransactionalService.class);
        beanFactory.registerBeanDefinition("testTransactionalService", testServiceDef);

        BeanDefinition classLevelServiceDef = new BeanDefinition("classLevelTransactionalService", ClassLevelTransactionalService.class);
        beanFactory.registerBeanDefinition("classLevelTransactionalService", classLevelServiceDef);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut); // 콘솔 출력 원복
        // 테스트 실패 시 캡처된 내용 확인용 (필요시 주석 해제)
        // System.out.println("--- Captured Test Output ---\n" + outContent.toString().trim() + "\n--- End Captured Test Output ---");
        outContent.reset(); // 다음 테스트를 위해 내용 초기화
    }

    @Test
    @DisplayName("성공: @Transactional 메소드 호출 시 트랜잭션 시작, 실행, 커밋")
    void transactionalMethod_Success() {
        TestTransactionalService service = beanFactory.getBean("testTransactionalService", TestTransactionalService.class);
        assertNotNull(service, "서비스 빈은 null이 아니어야 합니다.");
        assertInstanceOf(Factory.class, service, "서비스는 CGLIB 프록시 객체여야 합니다.");

        String result = service.doSomethingSuccess();
        assertEquals(TestTransactionalService.SUCCESS_MSG, result, "메소드 반환 값이 기대와 다릅니다.");

        String logs = outContent.toString();
        assertTrue(logs.contains("[DummyTransactionManager] Beginning transaction."), "트랜잭션이 시작되어야 합니다.");
        assertTrue(logs.contains("TestTransactionalService.doSomethingSuccess() executed"), "서비스 메소드가 실행되어야 합니다.");
        assertTrue(logs.contains("[DummyTransactionManager] Committing transaction."), "트랜잭션이 커밋되어야 합니다.");
        assertFalse(logs.contains("[DummyTransactionManager] Rolling back transaction."), "트랜잭션은 롤백되지 않아야 합니다.");
    }

    @Test
    @DisplayName("실패(롤백): @Transactional 메소드에서 예외 발생 시 트랜잭션 롤백")
    void transactionalMethod_Failure_ShouldRollback() {
        TestTransactionalService service = beanFactory.getBean("testTransactionalService", TestTransactionalService.class);
        assertNotNull(service);
        assertInstanceOf(Factory.class, service, "서비스는 프록시 객체여야 합니다.");

        Exception exception = assertThrows(RuntimeException.class, service::doSomethingFailure, "RuntimeException이 발생해야 합니다.");
        assertTrue(exception.getMessage().contains(TestTransactionalService.FAILURE_MSG_PART), "예외 메시지가 기대와 다릅니다.");

        String logs = outContent.toString();
        assertTrue(logs.contains("[DummyTransactionManager] Beginning transaction."), "트랜잭션이 시작되어야 합니다.");
        assertTrue(logs.contains("TestTransactionalService.doSomethingFailure() executed"), "서비스 메소드가 실행되어야 합니다.");
        assertTrue(logs.contains("[DummyTransactionManager] Rolling back transaction."), "트랜잭션이 롤백되어야 합니다.");
        assertFalse(logs.contains("[DummyTransactionManager] Committing transaction."), "트랜잭션은 커밋되지 않아야 합니다.");
    }

    @Test
    @DisplayName("트랜잭션 미적용: 프록시된 빈의 @Transactional 없는 메소드 호출 시 트랜잭션 로직 미실행")
    void nonTransactionalMethod_InProxiedBean_ShouldNotTriggerTx() {
        TestTransactionalService service = beanFactory.getBean("testTransactionalService", TestTransactionalService.class);
        assertNotNull(service);
        // 다른 메소드가 @Transactional이므로 빈 자체는 프록시될 수 있음
        assertInstanceOf(Factory.class, service, "서비스는 프록시 객체여야 합니다.");

        outContent.reset(); // 이전 로그(빈 생성 과정 등) 지우기

        String result = service.doSomethingNonTransactional();
        assertEquals("NON_TRANSACTIONAL_OK", result, "메소드 반환 값이 기대와 다릅니다.");

        String logs = outContent.toString();
        assertTrue(logs.contains("TestTransactionalService.doSomethingNonTransactional() executed"), "서비스 메소드가 실행되어야 합니다.");
        assertFalse(logs.contains("[DummyTransactionManager] Beginning transaction."), "트랜잭션은 시작되지 않아야 합니다.");
        assertFalse(logs.contains("[DummyTransactionManager] Committing transaction."), "트랜잭션은 커밋되지 않아야 합니다.");
        assertFalse(logs.contains("[DummyTransactionManager] Rolling back transaction."), "트랜잭션은 롤백되지 않아야 합니다.");
    }

    @Test
    @DisplayName("클래스 레벨 @Transactional: public 메소드 성공 시 트랜잭션 적용")
    void classLevelTransactional_PublicMethod_Success() {
        ClassLevelTransactionalService service = beanFactory.getBean("classLevelTransactionalService", ClassLevelTransactionalService.class);
        assertNotNull(service);
        assertInstanceOf(Factory.class, service, "클래스 레벨 @Transactional로 인해 서비스는 프록시여야 합니다.");

        String result = service.publicSuccessMethod();
        assertEquals(ClassLevelTransactionalService.CLASS_SUCCESS_MSG, result);

        String logs = outContent.toString();
        assertTrue(logs.contains("[DummyTransactionManager] Beginning transaction."));
        assertTrue(logs.contains("ClassLevelTransactionalService.publicSuccessMethod() executed"));
        assertTrue(logs.contains("[DummyTransactionManager] Committing transaction."));
        assertFalse(logs.contains("[DummyTransactionManager] Rolling back transaction."));
    }

    @Test
    @DisplayName("클래스 레벨 @Transactional: public 메소드 실패 시 트랜잭션 롤백")
    void classLevelTransactional_PublicMethod_Failure_ShouldRollback() {
        ClassLevelTransactionalService service = beanFactory.getBean("classLevelTransactionalService", ClassLevelTransactionalService.class);
        assertNotNull(service);
        assertInstanceOf(Factory.class, service, "서비스는 프록시 객체여야 합니다.");

        Exception exception = assertThrows(RuntimeException.class, service::publicFailureMethod);
        assertTrue(exception.getMessage().contains(ClassLevelTransactionalService.CLASS_FAILURE_MSG_PART));

        String logs = outContent.toString();
        assertTrue(logs.contains("[DummyTransactionManager] Beginning transaction."));
        assertTrue(logs.contains("ClassLevelTransactionalService.publicFailureMethod() executed"));
        assertTrue(logs.contains("[DummyTransactionManager] Rolling back transaction."));
        assertFalse(logs.contains("[DummyTransactionManager] Committing transaction."));
    }

    @Test
    @DisplayName("클래스 레벨 @Transactional: protected 메소드는 기본 매칭 규칙에 따라 트랜잭션 미적용")
    void classLevelTransactional_ProtectedMethod_ShouldNotBeTransactional() throws Exception { // 리플렉션 예외 처리
        ClassLevelTransactionalService service = beanFactory.getBean("classLevelTransactionalService", ClassLevelTransactionalService.class);
        assertNotNull(service);
        assertInstanceOf(Factory.class, service, "서비스는 클래스 레벨 어노테이션으로 인해 프록시여야 합니다.");

        outContent.reset();

        // 리플렉션을 사용하여 protected 메소드 호출
        java.lang.reflect.Method protectedMethod =
                ClassLevelTransactionalService.class.getDeclaredMethod("protectedMethod");
        protectedMethod.setAccessible(true); // protected 접근 제한 해제
        String result = (String) protectedMethod.invoke(service); // service 객체(프록시)의 메소드 실행

        assertEquals("PROTECTED_OK", result, "Protected 메소드 반환 값이 기대와 다릅니다.");

        String logs = outContent.toString();
        assertTrue(logs.contains("ClassLevelTransactionalService.protectedMethod() executed"), "Protected 메소드 자체는 실행되어야 합니다.");
        // AnnotationMethodMatcher는 클래스 레벨 어노테이션 시 public 메소드만 매칭하므로 트랜잭션 로직이 돌지 않아야 함
        assertFalse(logs.contains("[DummyTransactionManager] Beginning transaction."), "Protected 메소드는 기본적으로 트랜잭션 대상이 아닙니다.");
        assertFalse(logs.contains("[DummyTransactionManager] Committing transaction."), "트랜잭션 커밋 로그가 없어야 합니다.");
        assertFalse(logs.contains("[DummyTransactionManager] Rolling back transaction."), "트랜잭션 롤백 로그가 없어야 합니다.");
    }

    @Test
    @DisplayName("기존 트랜잭션 존재 시: 인터셉터는 새로운 트랜잭션을 시작하거나 종료하지 않음 (시뮬레이션)")
    void transactionalMethod_WithExistingActiveTransaction_Simulated() {
        DummyTransactionManager txManager = beanFactory.getBean("txManager", DummyTransactionManager.class);
        TestTransactionalService service = beanFactory.getBean("testTransactionalService", TestTransactionalService.class);

        // 외부에서 이미 트랜잭션이 시작되었다고 가정
        txManager.begin();
        assertTrue(txManager.isActive(), "외부 트랜잭션이 활성화되어 있어야 합니다.");
        outContent.reset(); // 수동으로 시작한 begin() 로그는 지움

        String result = service.doSomethingSuccess(); // 이 내부 호출에서는 새로운 트랜잭션을 시작하지 않아야 함
        assertEquals(TestTransactionalService.SUCCESS_MSG, result);

        String logs = outContent.toString();

        // TransactionInterceptor는 이미 활성 트랜잭션이 있으면 begin/commit/rollback을 호출하지 않음.
        assertFalse(logs.contains("[DummyTransactionManager] Beginning transaction."), "인터셉터는 새 트랜잭션을 시작하지 않아야 합니다.");
        assertTrue(logs.contains("TestTransactionalService.doSomethingSuccess() executed"), "서비스 메소드는 실행되어야 합니다.");
        assertFalse(logs.contains("[DummyTransactionManager] Committing transaction."), "인터셉터는 외부 트랜잭션을 커밋하지 않아야 합니다.");
        assertFalse(logs.contains("[DummyTransactionManager] Rolling back transaction."), "인터셉터는 외부 트랜잭션을 롤백하지 않아야 합니다.");

        // DummyTransactionManager 자체의 중복 begin() 호출 시 경고 로그가 없는지 확인
        // (인터셉터가 isActive()를 잘 체크해서 중복 호출을 안했다는 의미)
        assertFalse(logs.contains("Transaction is already active."), "DummyTransactionManager의 begin()이 중복 호출되지 않아야 합니다.");

        // 외부 트랜잭션 수동 커밋
        txManager.commit();
        assertFalse(txManager.isActive(), "외부 트랜잭션이 커밋되어 비활성화되어야 합니다.");
    }
}