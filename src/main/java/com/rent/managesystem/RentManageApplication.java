package com.rent.managesystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 租房管理系统启动类
 *
 * <p>这是整个后端服务的入口，Spring Boot 会从这里开始：
 * <ol>
 *   <li>扫描当前包（com.rent.managesystem）及其所有子包中的 Spring Bean</li>
 *   <li>自动配置内嵌的 Tomcat 服务器（默认端口 8080）</li>
 *   <li>根据 pom.xml 中的依赖进行自动装配</li>
 * </ol>
 *
 * <p><b>包扫描范围：</b>{@code com.rent.managesystem} 及其子包
 *
 * @author 连梓祺 & 团队
 * @version 1.0.0
 * @since 2026-05-10
 */
@SpringBootApplication
public class RentManageApplication {

    /**
     * 应用程序主入口方法
     *
     * @param args 命令行参数（可通过 --server.port=8081 等方式覆盖默认配置）
     */
    public static void main(String[] args) {
        SpringApplication.run(RentManageApplication.class, args);
        System.out.println("============================================");
        System.out.println("   🏠 青年品质租房管理系统 启动成功！");
        System.out.println("   访问地址: http://localhost:8080");
        System.out.println("   API文档: http://localhost:8080/api/doc.html");
        System.out.println("   测试账号 13811111111 / 123456");
        System.out.println("============================================");
    }
    
}