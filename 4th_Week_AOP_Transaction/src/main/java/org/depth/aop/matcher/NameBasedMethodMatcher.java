package org.depth.aop.matcher;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class NameBasedMethodMatcher implements MethodMatcher {
    private final List<String> mappedNames = new ArrayList<>();

    public NameBasedMethodMatcher() {}

    public NameBasedMethodMatcher(String... mappedNames) {
        addMappedNames(mappedNames);
    }

    public void addMappedName(String mappedName) {
        if (mappedName != null && !mappedName.isEmpty()) {
            this.mappedNames.add(mappedName);
        }
    }

    public void addMappedNames(String... mappedNames) {
        if (mappedNames != null) {
            for (String name : mappedNames) {
                addMappedName(name);
            }
        }
    }

    public void setMappedNames(String... mappedNames) {
        this.mappedNames.clear();
        addMappedNames(mappedNames);
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        if (this.mappedNames.isEmpty()) {
            // 이름이 지정되지 않은 경우, 이름 기준으로는 모든 메서드와 일치 (다른 필터가 있다면 그에 따름)
            // 또는, false를 반환하여 명시적인 이름 매칭만 허용할 수도 있습니다.
            // 여기서는 이름 목록이 비어있으면 매칭되지 않는 것으로 처리하겠습니다. (명시적 매칭)
            return false;
        }
        String methodName = method.getName();
        for (String mappedName : this.mappedNames) {
            if (isMatch(methodName, mappedName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 주어진 메서드 이름이 매핑된 이름과 일치하는지 확인합니다.
     * 기본 구현은 "xxx*", "*xxx", "xxx" 매칭을 확인합니다.
     * 필요시 하위 클래스에서 재정의할 수 있습니다.
     * @param methodName 클래스의 메서드 이름
     * @param mappedName 매핑된 이름 패턴
     * @return 이름이 일치하면 true
     */
    protected boolean isMatch(String methodName, String mappedName) {
        if (mappedName.endsWith("*")) {
            return methodName.startsWith(mappedName.substring(0, mappedName.length() - 1));
        } else if (mappedName.startsWith("*")) {
            return methodName.endsWith(mappedName.substring(1));
        } else {
            return methodName.equals(mappedName);
        }
    }
}