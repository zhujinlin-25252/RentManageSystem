package com.rent.managesystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 租房管理系统单元测试基类
 *
 * <p>使用 {@code @SpringBootTest} 注解加载完整的 Spring 上下文，
 * 可以进行集成测试（测试Controller/Service/Mapper各层协作）。
 *
 * <p><b>运行方式：</b>
 * <ul>
 *   <li>Idea 中右键 → Run</li>
 *   <li>Maven: mvn test</li>
 * </ul>
 *
 * @author 连梓祺 & 团队
 */
@SpringBootTest
class RentManageApplicationTests {

    @Test
    void contextLoads() {
        // 如果这个测试通过，说明 Spring Boot 能正常启动
        // 后续可以在这里添加更多测试用例
    }

}
