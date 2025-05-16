package org.depth.beans.testbeans;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class TestBeanWithNoDeps {
    private String id = "defaultId";

    public TestBeanWithNoDeps(String id) {
        this.id = id;
    }

    public void greet() {
        System.out.println("Hello from TestBeanWithNoDeps, id: " + id);
    }
}