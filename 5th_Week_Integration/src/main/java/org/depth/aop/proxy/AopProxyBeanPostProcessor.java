package org.depth.aop.proxy;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import org.depth.aop.Advisor;
import org.depth.aop.AroundAdvice;
import org.depth.aop.Pointcut;
import org.depth.beans.BeanDefinition;
import org.depth.beans.factory.BeanFactory;
import org.depth.beans.factory.ListableBeanFactory;
import org.depth.beans.factory.config.BeanPostProcessor;
import org.depth.beans.factory.exception.BeansException;
import org.depth.transaction.TransactionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
public class AopProxyBeanPostProcessor implements BeanPostProcessor {
    private BeanFactory beanFactory;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean == null) {
            return null;
        }

        if (isInfrastructureClass(bean.getClass())) {
            return bean;
        }

        List<Advisor> applicableAdvisors = findApplicableAdvisors(bean, beanName);

        if (!applicableAdvisors.isEmpty()) {
            return createProxy(bean, beanName, applicableAdvisors);
        }

        return bean; // 프록시가 필요 없는 경우 원본 객체 반환
    }

    protected List<Advisor> findApplicableAdvisors(Object bean, String beanName) {
        List<Advisor> candidates = new ArrayList<>();
        if (beanFactory instanceof ListableBeanFactory lbf) {
            String[] advisorNames = lbf.getBeanDefinitionNames(); // 모든 빈 이름을 가져옴
            for (String name : advisorNames) {
                BeanDefinition bd = lbf.getBeanDefinition(name);
                if (Advisor.class.isAssignableFrom(bd.getBeanClass())) {
                    // Advisor 빈 자체를 프록시 처리하지 않도록 주의 (isInfrastructureClass에서 일부 처리)
                    if (!name.equals(beanName)) { // 자기 자신을 프록시하지 않도록
                        try {
                            candidates.add((Advisor) beanFactory.getBean(name));
                        } catch (BeansException e) {
                            System.err.println("AOP: Error getting advisor bean '" + name + "'. Skipping. Error: " + e.getMessage());
                        }
                    }
                }
            }
        } else {
            // ListableBeanFactory가 아닌 경우 Advisor를 찾는 다른 방법 필요
            System.err.println("AOP: Cannot find advisors, BeanFactory is not ListableBeanFactory.");
        }


        // Pointcut 매칭
        Class<?> targetClass = bean.getClass();
        return candidates.stream()
                .filter(advisor -> advisor.getPointcut().getClassMatcher().matches(targetClass))
                .collect(Collectors.toList());
    }

    private boolean isInfrastructureClass(Class<?> beanClass) {
        return Advisor.class.isAssignableFrom(beanClass) ||
                Pointcut.class.isAssignableFrom(beanClass) ||
                AroundAdvice.class.isAssignableFrom(beanClass) ||
                BeanPostProcessor.class.isAssignableFrom(beanClass) ||
                TransactionManager.class.isAssignableFrom(beanClass);
    }

    protected Object createProxy(Object targetBean, String beanName, List<Advisor> allAdvisorsForBean) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetBean.getClass());

        if (allAdvisorsForBean.isEmpty()) {
            return targetBean; // 적용할 Advisor가 없으면 원본 빈 반환
        }

        // Pointcut을 동적으로 확인하고 Advice 체인을 실행할 수 있는 새로운 MethodInterceptor를 생성
        MethodInterceptor interceptor = new DynamicAdvisedInterceptor(targetBean, allAdvisorsForBean);
        enhancer.setCallback(interceptor); // 이 단일 인터셉터가 모든 호출을 받아 동적으로 처리

        return enhancer.create();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

}
