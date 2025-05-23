package org.depth;

import org.depth.web.annotation.Controller;
import org.depth.web.annotation.RequestMapping;

@Controller
public class TestController {
    @RequestMapping("/hello")
    public String sayHello() {
        return "Hello, World!";
    }

    @RequestMapping("/bye")
    public String sayBye() {
        return "Bye, World!";
    }
}
