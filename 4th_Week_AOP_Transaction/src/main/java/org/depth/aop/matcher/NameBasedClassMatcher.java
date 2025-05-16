package org.depth.aop.matcher;


import java.util.ArrayList;
import java.util.List;

public class NameBasedClassMatcher implements ClassMatcher {
    private final List<String> mappedNames = new ArrayList<>();

    public NameBasedClassMatcher() {}

    public NameBasedClassMatcher(String... mappedNames) {
        setMappedNames(mappedNames);
    }

    public void addMappedName(String mappedName) {
        if (mappedName != null && !mappedName.isEmpty()) {
            this.mappedNames.add(mappedName);
        }
    }

    public void setMappedNames(String... mappedNames) {
        this.mappedNames.clear();
        if (mappedNames != null) {
            for (String name : mappedNames) {
                addMappedName(name);
            }
        }
    }

    @Override
    public boolean matches(Class<?> clazz) {
        if (this.mappedNames.isEmpty()) {
            return false; // 이름이 지정되지 않으면 매칭되지 않음
        }
        String className = clazz.getName(); // 정규화된 클래스 이름 사용
        for (String mappedName : this.mappedNames) {
            if (isMatch(className, mappedName)) {
                return true;
            }
        }
        return false;
    }

    // NameBasedMethodMatcher의 패턴 매칭 로직 재사용 또는 유사하게 구현
    protected boolean isMatch(String name, String mappedName) {
        if (mappedName.endsWith("*")) {
            if (name.startsWith(mappedName.substring(0, mappedName.length() - 1))) {
                return true;
            }
        } else if (mappedName.startsWith("*")) {
            if (name.endsWith(mappedName.substring(1))) {
                return true;
            }
        } else {
            if (name.equals(mappedName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "NameBasedClassMatcher [mappedNames=" + mappedNames + "]";
    }
}