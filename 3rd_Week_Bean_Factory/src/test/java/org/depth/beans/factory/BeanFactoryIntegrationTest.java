package org.depth.beans.factory;

import org.depth.beans.factory.exception.BeanCreationException;
import org.depth.beans.factory.exception.NoSuchBeanDefinitionException;
import org.depth.beans.factory.support.XmlBeanDefinitionReader;
import org.depth.beans.testbeans.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BeanFactoryIntegrationTest {

    private ListableBeanFactory beanFactory;
    private XmlBeanDefinitionReader reader;

    // 테스트 XML 파일 경로 (클래스패스 기준)
    private final String TEST_BEANS_XML = "test-beans.xml";
    private final String CIRCULAR_BEANS_XML = "test-dependency-beans.xml";


    @BeforeEach
    void setUp() {
        beanFactory = new ListableBeanFactory();
        reader = new XmlBeanDefinitionReader(beanFactory);
    }

    @Nested
    @DisplayName("기본 빈 로딩 및 조회 테스트")
    class BasicBeanLoadingTests {

        @BeforeEach
        void loadBasicBeans() {
            reader.loadBeanDefinitions(TEST_BEANS_XML);
        }

        @Test
        @DisplayName("의존성 없는 빈을 정상적으로 조회해야 한다")
        void getBean_withNoDependencies_shouldReturnInstance() {
            TestBeanWithNoDeps noDepsBean = beanFactory.getBean("noDepsBean", TestBeanWithNoDeps.class);
            assertThat(noDepsBean).isNotNull();
            noDepsBean.setId("customId"); // Setter로 값 변경 테스트
            assertThat(noDepsBean.getId()).isEqualTo("customId");
            noDepsBean.greet();
        }

        @Test
        @DisplayName("생성자 주입으로 의존성이 해결된 빈을 조회해야 한다")
        void getBean_withConstructorInjection_shouldResolveDependencies() {
            TestBeanB beanB = beanFactory.getBean("testB", TestBeanB.class);
            assertThat(beanB).isNotNull();
            assertThat(beanB.getBeanC()).isNotNull();
            assertThat(beanB.getBeanC()).isInstanceOf(TestBeanC.class);
            beanB.doSomethingInB();
        }

        @Test
        @DisplayName("Setter 주입으로 의존성이 해결된 빈을 조회해야 한다")
        void getBean_withSetterInjection_shouldResolveDependencies() {
            TestBeanA beanA = beanFactory.getBean("testA", TestBeanA.class);
            assertThat(beanA).isNotNull();
            assertThat(beanA.getBeanB()).isNotNull();
            assertThat(beanA.getBeanC()).isNotNull(); // Setter로 주입된 beanC
            assertThat(beanA.getBeanC().getName()).isNull(); // testCWithName의 name property는 value 주입 미구현으로 null

            // testCWithName 빈 직접 확인 (Setter 주입 확인용)
            TestBeanC beanCWithName = beanFactory.getBean("testCWithName", TestBeanC.class);
            assertThat(beanCWithName).isNotNull();
            // 현재 value property 주입이 안되므로, setter로 직접 설정 후 테스트
            beanCWithName.setName("Manually Set Name for C");
            assertThat(beanCWithName.getName()).isEqualTo("Manually Set Name for C");

            // beanA에 주입된 beanC가 testCWithName과 동일한 인스턴스인지 확인 (싱글톤)
            assertThat(beanA.getBeanC()).isSameAs(beanCWithName);

            beanA.doSomethingInA();
        }

        @Test
        @DisplayName("같은 빈을 여러 번 조회하면 동일한 인스턴스(싱글톤)를 반환해야 한다")
        void getBean_multipleTimes_shouldReturnSameInstance() {
            TestBeanC beanC1 = beanFactory.getBean("testC", TestBeanC.class);
            TestBeanC beanC2 = beanFactory.getBean("testC", TestBeanC.class);
            assertThat(beanC1).isNotNull();
            assertThat(beanC1).isSameAs(beanC2);
        }

        @Test
        @DisplayName("존재하지 않는 빈을 조회하면 NoSuchBeanDefinitionException 발생")
        void getBean_nonExistentBean_shouldThrowNoSuchBeanDefinitionException() {
            assertThatThrownBy(() -> beanFactory.getBean("nonExistentBean"))
                    .isInstanceOf(NoSuchBeanDefinitionException.class);
        }

        @Test
        @DisplayName("잘못된 타입으로 빈을 조회하면 BeanCreationException 발생")
        void getBean_withWrongType_shouldThrowBeanCreationException() {
            assertThatThrownBy(() -> beanFactory.getBean("testA", TestBeanB.class)) // testA는 TestBeanA 타입
                    .isInstanceOf(BeanCreationException.class);
        }
    }

    @Nested
    @DisplayName("ListableBeanFactory 기능 테스트")
    class ListableFunctionTests {
        @BeforeEach
        void loadBasicBeans() {
            reader.loadBeanDefinitions(TEST_BEANS_XML);
        }

        @Test
        @DisplayName("getBeanDefinitionCount는 등록된 빈의 개수를 반환해야 한다")
        void getBeanDefinitionCount_shouldReturnCorrectCount() {
            // test-beans.xml에는 noDepsBean, testC, testCWithName, testB, testA (총 5개)
            assertThat(beanFactory.getBeanDefinitionCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("getBeanDefinitionNames는 모든 빈의 이름을 반환해야 한다")
        void getBeanDefinitionNames_shouldReturnAllBeanNames() {
            String[] names = beanFactory.getBeanDefinitionNames();
            assertThat(names).containsExactlyInAnyOrder("noDepsBean", "testC", "testCWithName", "testB", "testA");
        }

        @Test
        @DisplayName("containsBeanDefinition은 빈 정의 존재 여부를 정확히 반환해야 한다")
        void containsBeanDefinition_shouldReturnCorrectly() {
            assertThat(beanFactory.containsBeanDefinition("testA")).isTrue();
            assertThat(beanFactory.containsBeanDefinition("nonExistentBean")).isFalse();
        }
    }


    @Nested
    @DisplayName("순환 참조 테스트")
    class CircularDependencyTests {

        @Test
        @DisplayName("생성자 주입 간 순환 참조 발생 시 BeanCreationException을 던져야 한다")
        void constructorCircularDependency_shouldThrowBeanCreationException() {
            reader.loadBeanDefinitions(CIRCULAR_BEANS_XML);

            assertThatThrownBy(() -> beanFactory.getBean("circularA", CircularDepBeanA.class))
                    .isInstanceOf(BeanCreationException.class)
                    .hasMessageContaining("Circular dependency detected for bean 'circularA'");
        }
    }
}