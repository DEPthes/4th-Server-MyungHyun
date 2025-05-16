package org.depth.aop;

import net.sf.cglib.proxy.Factory;
import org.depth.aop.beans.*;
import org.depth.aop.proxy.AopProxyBeanPostProcessor;
import org.depth.beans.BeanDefinition;
import org.depth.beans.factory.ListableBeanFactory;
// Advice, Advisor, Pointcut 인터페이스는 프레임워크의 것을 사용 (org.depth.aop.*)

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

// BeanFactory와 AOP 기능 통합 테스트
public class AopBeanFactoryTest {

    private ListableBeanFactory beanFactory;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent)); // 콘솔 출력 캡처 시작

        beanFactory = new ListableBeanFactory();

        // 1. AOP PostProcessor 등록
        AopProxyBeanPostProcessor aopProcessor = new AopProxyBeanPostProcessor(beanFactory);
        beanFactory.addBeanPostProcessor(aopProcessor);

        // 2. Advisor 빈 등록 (SimpleAdvisor는 기본 생성자에서 Pointcut과 Advice를 내부적으로 생성)
        BeanDefinition advisorDef = new BeanDefinition("simpleAdvisor", SimpleAdvisor.class);
        beanFactory.registerBeanDefinition("simpleAdvisor", advisorDef);
        // AopProxyBeanPostProcessor가 BeanFactory에서 "simpleAdvisor" 빈을 찾아 AOP에 사용합니다.

        // 3. 테스트용 서비스 빈들 등록
        BeanDefinition myServiceDef = new BeanDefinition("myService", MyService.class);
        myServiceDef.addPropertyBeanName("dependency", "dependencyBean"); // Setter DI 설정
        beanFactory.registerBeanDefinition("myService", myServiceDef);

        beanFactory.registerBeanDefinition("anotherService", new BeanDefinition("anotherService", AnotherService.class));
        beanFactory.registerBeanDefinition("dependencyBean", new BeanDefinition("dependencyBean", DependencyBean.class));

        BeanDefinition clientSetterDef = new BeanDefinition("clientBeanSetter", ClientBean.class);
        clientSetterDef.addPropertyBeanName("myService", "myService"); // Setter DI
        beanFactory.registerBeanDefinition("clientBeanSetter", clientSetterDef);

        BeanDefinition clientConstructorDef = new BeanDefinition("clientBeanConstructor", ClientBean.class);
        clientConstructorDef.addConstructorArgBeanName("myService"); // Constructor DI
        beanFactory.registerBeanDefinition("clientBeanConstructor", clientConstructorDef);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut); // 콘솔 출력 원복
        // 캡처된 내용 확인 (디버깅 시 유용)
        // System.out.println("--- Captured Test Output ---\n" + outContent.toString().trim() + "\n--- End Captured Test Output ---");
    }

    @Test
    @DisplayName("AOP 미적용 빈 생성 및 호출 테스트")
    void beanCreationWithoutAop() {
        AnotherService anotherService = beanFactory.getBean("anotherService", AnotherService.class);
        assertNotNull(anotherService);
        assertFalse(anotherService instanceof Factory, "AnotherService는 프록시가 아니어야 함.");
        assertEquals("Hello from AnotherService", anotherService.greet());
        assertTrue(outContent.toString().contains("AnotherService: greet executed"));
    }

    @Test
    @DisplayName("AOP 적용 빈 생성 및 Pointcut 매칭 메서드 호출 테스트")
    void beanCreationWithAop_MatchingMethod() {
        MyService myService = beanFactory.getBean("myService", MyService.class);
        assertNotNull(myService);
        assertInstanceOf(Factory.class, myService, "MyService는 CGLIB 프록시여야 함.");

        String result = myService.performAction("AOP_Param");
        String logs = outContent.toString();

        assertTrue(logs.contains("[AOP LOG] BEFORE: MyService.performAction with args [AOP_Param]"));
        assertTrue(logs.contains("MyService: performAction executed with param - AOP_Param"));
        assertTrue(logs.contains("DependencyBean: doSomething executed"));
        assertTrue(logs.contains("[AOP LOG] AFTER_RETURNING: MyService.performAction, result: MyService: Result for AOP_Param"));
        assertEquals("MyService: Result for AOP_Param", result, "Advice에 의해 반환값 조작 필요.");
    }

    @Test
    @DisplayName("AOP 적용 빈의 Pointcut 미매칭 메서드 호출 테스트")
    void beanCreationWithAop_NonMatchingMethod() {
        MyService myService = beanFactory.getBean("myService", MyService.class); // 프록시 객체
        assertInstanceOf(Factory.class, myService);
        outContent.reset(); // 이전 로그 지우기

        myService.anotherAction(); // Pointcut에 매칭되지 않는 메서드 (SimpleAdvisor 기본 설정 기준)
        String logs = outContent.toString();

        assertFalse(logs.contains("[AOP LOG]"), "anotherAction 호출 시 AOP 로그가 없어야 함.");
        assertTrue(logs.contains("MyServiceImpl: anotherAction executed"));
    }

    @Test
    @DisplayName("AOP 프록시 빈의 Setter 의존성 주입 테스트")
    void aopProxyDependencyInjection_Setter() {
        ClientBean client = beanFactory.getBean("clientBeanSetter", ClientBean.class);
        assertNotNull(client.getMyService());
        assertInstanceOf(Factory.class, client.getMyService(), "주입된 MyService는 프록시여야 함.");
        outContent.reset();

        String serviceResult = client.executeServiceTask("SetterDI_Test");
        assertTrue(outContent.toString().contains("[AOP LOG]"));
        assertEquals("MyService: Result for SetterDI_Test", serviceResult);
    }

    @Test
    @DisplayName("AOP 프록시 빈의 생성자 의존성 주입 테스트")
    void aopProxyDependencyInjection_Constructor() {
        ClientBean client = beanFactory.getBean("clientBeanConstructor", ClientBean.class);
        assertNotNull(client.getMyService());
        assertInstanceOf(Factory.class, client.getMyService(), "주입된 MyService는 프록시여야 함.");
        outContent.reset();

        String serviceResult = client.executeServiceTask("ConstructorDI_Test");
        assertTrue(outContent.toString().contains("[AOP LOG]"));
        assertEquals("MyService: Result for ConstructorDI_Test", serviceResult);
    }

    @Test
    @DisplayName("AOP 프록시 빈의 싱글톤 스코프 확인")
    void singletonScopeWithAop() {
        MyService service1 = beanFactory.getBean("myService", MyService.class);
        MyService service2 = beanFactory.getBean("myService", MyService.class);
        assertSame(service1, service2, "AOP 프록시 빈은 싱글톤이어야 함.");
    }

    @Test
    @DisplayName("프록시된 빈 내부의 의존성 동작 확인")
    void dependencyBehaviorInProxiedBean() {
        MyService myService = beanFactory.getBean("myService", MyService.class);
        DependencyBean test = myService.getDependency();
        assertNotNull(myService.getDependency(), "의존성(DependencyBean)이 주입되어야 함.");
        outContent.reset();

        myService.performAction("DepTest"); // 이 호출로 DependencyBean.doSomething()이 실행되어야 함
        assertTrue(outContent.toString().contains("DependencyBean: doSomething executed"), "의존성 빈의 메서드가 호출되어야 함.");
    }
}