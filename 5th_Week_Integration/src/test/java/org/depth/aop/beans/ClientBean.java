package org.depth.aop.beans;

// 서비스 빈을 사용하는 클라이언트 빈
public class ClientBean {
    private MyService myService;

    // 기본 생성자 (BeanFactory가 사용할 수 있도록)
    public ClientBean() {}

    // 생성자 주입용
    public ClientBean(MyService myService) {
        this.myService = myService;
    }

    public void setMyService(MyService myService) {
        this.myService = myService;
    }

    public MyService getMyService() {
        return myService;
    }

    public String executeServiceTask(String param) {
        System.out.println("ClientBean: Executing service task with param - " + param);
        return myService.performAction(param);
    }
}