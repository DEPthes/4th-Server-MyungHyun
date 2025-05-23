package org.depth.beans.factory;

import org.depth.beans.BeanDefinition;
import org.depth.beans.factory.config.BeanPostProcessor;
import org.depth.beans.factory.exception.BeanCreationException;
import org.depth.beans.factory.exception.BeanInstantiationException;
import org.depth.beans.factory.exception.BeansException;
import org.depth.beans.factory.exception.NoSuchBeanDefinitionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ListableBeanFactory implements BeanFactory, BeanDefinitionRegistry {
    private final Map<String, BeanDefinition> beanDefinitions = new ConcurrentHashMap<>();
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private final ThreadLocal<Set<String>> currentlyInCreation = ThreadLocal.withInitial(HashSet::new);
    private final List<BeanPostProcessor> beanPostProcessors = new CopyOnWriteArrayList<>();



    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        this.beanPostProcessors.remove(beanPostProcessor);
        this.beanPostProcessors.add(beanPostProcessor);
    }

    /**
     * 빈 인스턴스를 반환하는 메서드
     * @param name 빈 이름
     * @return 빈 인스턴스
     * @throws BeansException 빈 생성 중 발생하는 예외
     */
    @Override
    public Object getBean(String name) throws BeansException {
        return doGetBean(name, null);
    }

    /**
     * 빈 인스턴스를 반환하는 메서드
     * @param <T> 빈 타입
     * @param name 빈 이름
     * @param requiredType 빈 타입
     * @return 빈 인스턴스
     * @throws BeansException 빈 생성 중 발생하는 예외
     */
    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return doGetBean(name, requiredType);
    }

    /**
     * 빈 정의가 존재하는지 확인하는 메서드
     * @param name 빈 이름
     * @return 빈 정의가 존재하면 true, 존재하지 않으면 false
     */
    @Override
    public boolean containsBean(String name) {
        return beanDefinitions.containsKey(name);
    }

    /**
     * 빈 생성 로직을 구현한 메서드
     * @param name 빈 이름
     * @param requiredType 빈 타입
     * @return 빈 인스턴스
     * @param <T> 빈 타입
     * @throws BeansException 빈 생성 중 발생하는 예외
     */
    @SuppressWarnings("unchecked")
    protected <T> T doGetBean(final String name, final Class<T> requiredType) throws BeansException {
        Object sharedInstance = singletonObjects.get(name);

        if (sharedInstance != null) {
            // 이미 생성된 빈(프록시일 수 있음)이 타입과 맞는지 확인
            if (requiredType != null && !requiredType.isInstance(sharedInstance)) {
                throw new BeanCreationException(name, "Bean is not of required type: " + requiredType.getName() +
                        ". Existing bean type: " + sharedInstance.getClass().getName());
            }
            return (T) sharedInstance;
        }

        if (currentlyInCreation.get().contains(name)) {
            throw new BeanCreationException(name,
                    "Circular dependency detected for bean '" + name + "'. Path: " +
                            String.join(" -> ", currentlyInCreation.get()) + " -> " + name);
        }

        final BeanDefinition bd = beanDefinitions.get(name);
        if (bd == null) {
            throw new NoSuchBeanDefinitionException(name);
        }

        currentlyInCreation.get().add(name);
        Object beanInstance;
        try {
            Object createdBean = createBean(name, bd);
            beanInstance = initializeBean(name, createdBean, bd);
            singletonObjects.put(name, beanInstance);

        } catch (BeansException e) {
            throw e;
        } catch (Exception e) {
            throw new BeanCreationException(name, "Error creating bean", e);
        } finally {
            currentlyInCreation.get().remove(name);
            if (currentlyInCreation.get().isEmpty()) {
                currentlyInCreation.remove();
            }
        }

        if (requiredType != null && !requiredType.isInstance(beanInstance)) {
            throw new BeanCreationException(name, "Bean is not of required type: " + requiredType.getName() +
                    ". Final bean type: " + beanInstance.getClass().getName());
        }
        return (T) beanInstance;
    }

    protected Object initializeBean(String beanName, Object bean, BeanDefinition bd) throws BeansException {
        Object wrappedBean = bean;

        // 1. postProcessBeforeInitialization 호출
        for (BeanPostProcessor processor : this.beanPostProcessors) {
            wrappedBean = processor.postProcessBeforeInitialization(wrappedBean, beanName);
            if (wrappedBean == null) {
                // 후처리기가 null을 반환하면, 더 이상 처리하지 않고 null을 반환 (Spring 참조)
                return null;
            }
        }

        // 2. 사용자 정의 초기화 메서드 호출 (예: InitializingBean 인터페이스 구현 또는 init-method 속성)
        // invokeInitMethods(beanName, wrappedBean, bd); // (선택적 구현)

        // 3. postProcessAfterInitialization 호출 (AOP 프록시 생성의 주요 지점)
        for (BeanPostProcessor processor : this.beanPostProcessors) {
            wrappedBean = processor.postProcessAfterInitialization(wrappedBean, beanName);
            if (wrappedBean == null) {
                return null;
            }
        }

        return wrappedBean;
    }

    /**
     * 빈을 생성하는 메서드
     * @param beanName 빈 이름
     * @param bd 빈 정의
     * @return 생성된 빈 인스턴스
     * @throws BeansException 빈 생성 중 발생하는 예외
     */
    protected Object createBean(String beanName, BeanDefinition bd) throws BeansException {
        try {
            Object beanInstance = instantiateBean(beanName, bd);
            populateBean(beanName, bd, beanInstance);

            return beanInstance;
        }catch (BeansException e) {
            throw e;
        }catch (Exception e) {
            throw new BeanCreationException(beanName, "Error creating bean", e);
        }
    }

    /**
     * 빈에게 의존성을 주입하는 메서드
     * @param beanName 빈 이름
     * @param bd 빈 정의
     * @return 생성된 빈 인스턴스
     * @throws BeansException 빈 생성 중 발생하는 예외
     */
    protected Object instantiateBean(String beanName, BeanDefinition bd) throws BeansException {
        Class<?> beanClass = bd.getBeanClass();
        try {
            if(bd.getConstructorArgBeanNames().isEmpty()) { // 기본 생성자 사용
                Constructor<?> constructor = beanClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            } else {
                List<Object> args = new ArrayList<>();
                List<Class<?>> argTypes = new ArrayList<>();

                for(String argBeanName : bd.getConstructorArgBeanNames()) {
                    Object argBean = getBean(argBeanName);
                    args.add(argBean);
                    argTypes.add(argBean.getClass());
                }

                Constructor<?> constructor = findMatchingConstructor(beanClass, argTypes);
                if(constructor == null) {
                    throw new BeanInstantiationException(beanName, "No matching constructor found");
                }
                constructor.setAccessible(true);
                return constructor.newInstance(args.toArray());
            }
        }catch (NoSuchMethodException e) {
            throw new BeanInstantiationException(beanName, "No default constructor found");
        }catch (BeansException e) {
            throw e;
        }catch (Exception e) {
            throw new BeanInstantiationException(beanName, "Error instantiating bean", e);
        }
    }

    /**
     * 일치하는 생성자를 찾아 반환하는 메서드
     * @param beanClass 빈 클래스
     * @param argTypes 생성자 인자 타입 목록
     * @return 일치하는 생성자 (없으면 null)
     */
    private Constructor<?> findMatchingConstructor(Class<?> beanClass, List<Class<?>> argTypes) {
        for(Constructor<?> constructor : beanClass.getDeclaredConstructors()) {
            Class<?>[] paramTypes = constructor.getParameterTypes();
            if(paramTypes.length == argTypes.size()) {
                boolean match = true;
                for(int i = 0; i < paramTypes.length; i++) {
                    if(!paramTypes[i].isAssignableFrom(argTypes.get(i))) {
                        match = false;
                        break;
                    }
                }
                if(match) {
                    return constructor;
                }
            }
        }
        return null;
    }

    /**
     * 빈에게 의존성을 주입하는 메서드
     * @param beanName 빈 이름
     * @param bd 빈 정의
     * @param beanInstance 빈 인스턴스
     * @throws BeansException 빈 생성 중 발생하는 예외
     */
    protected void populateBean(String beanName, BeanDefinition bd, Object beanInstance) throws BeansException {
        Map<String, String> propertyBeanNames = bd.getPropertyBeanNames();
        if (propertyBeanNames.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> entry : propertyBeanNames.entrySet()) {
            String propertyName = entry.getKey();
            String dependentBeanName = entry.getValue();

            try {
                Object dependentBean = getBean(dependentBeanName); // 의존성 빈 가져오기

                // Setter 메소드 이름 생성 (e.g., "name" -> "setName")
                String setterName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);

                // Setter 메소드 찾기 (인자 타입 고려해야 함)
                Method setterMethod = findSetterMethod(beanInstance.getClass(), setterName, dependentBean.getClass());
                if (setterMethod == null) {
                    throw new BeanCreationException(beanName, "No suitable setter method '" + setterName +
                            "' found for property '" + propertyName + "' with type " + dependentBean.getClass().getName());
                }

                setterMethod.setAccessible(true);
                setterMethod.invoke(beanInstance, dependentBean);

            } catch (BeansException e) {
                throw e;
            } catch (Exception e) {
                throw new BeanCreationException(beanName, "Failed to set property '" + propertyName + "'", e);
            }
        }
    }

    /**
     * 일치하는 setter 메소드를 찾아 반환하는 메서드
     * @param beanClass 빈 클래스
     * @param methodName setter 메소드 이름
     * @param argType setter 메소드 인자 타입
     * @return 일치하는 setter 메소드 (없으면 null)
     */
    private Method findSetterMethod(Class<?> beanClass, String methodName, Class<?> argType) {
        for (Method method : beanClass.getMethods()) { // public 메소드만 검색
            if (method.getName().equals(methodName) && method.getParameterCount() == 1) {
                if (method.getParameterTypes()[0].isAssignableFrom(argType)) {
                    return method;
                }
            }
        }
        //TODO: 상위 클래스/인터페이스의 메소드도 찾기
        return null; 
    }

    /**
     * 빈 정의를 등록하는 메서드
     * @param beanName 빈 이름
     * @param beanDefinition 빈 정의
     */
    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        if (this.beanDefinitions.containsKey(beanName)) {
            System.err.println("Bean definition with name '" + beanName + "' already registered. Skipping.");
            return;
        }
        this.beanDefinitions.put(beanName, beanDefinition);
    }

    /**
     * 빈 정의를 가져오는 메서드
     * @param beanName 빈 이름
     * @return 빈 정의
     * @throws NoSuchBeanDefinitionException 빈 정의가 존재하지 않을 경우 발생하는 예외
     */
    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        BeanDefinition bd = this.beanDefinitions.get(beanName);
        if (bd == null) {
            throw new NoSuchBeanDefinitionException(beanName);
        }
        return bd;
    }

    /**
     * 빈 정의가 존재하는지 확인하는 메서드
     * @param beanName 빈 이름
     * @return 빈 정의가 존재하면 true, 존재하지 않으면 false
     */
    @Override
    public boolean containsBeanDefinition(String beanName) {
        return this.beanDefinitions.containsKey(beanName);
    }

    @Override
    public int getBeanDefinitionCount() {
        return this.beanDefinitions.size();
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return this.beanDefinitions.keySet().toArray(new String[0]);
    }
}
