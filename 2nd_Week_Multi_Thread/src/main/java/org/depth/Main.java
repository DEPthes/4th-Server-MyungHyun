package org.depth;

import org.depth.container.ServletContainer;

public class Main {
    public static void main(String[] args) {
        int port = 8080;

        // 서블릿 컨테이너 생성
        ServletContainer container = new ServletContainer(port);

        // 서블릿 등록
        container.addServlet("/", new CustomServlet());

        // 컨테이너 시작
        container.start();

        System.out.println("서블릿 컨테이너가 " + port + " 포트에서 실행 중입니다.");
        System.out.println("종료하려면 엔터 키를 누르세요...");

        try {
            System.in.read();
            // 컨테이너 종료
            container.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}