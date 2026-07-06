package com.rent.managesystem.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 *
 * <p>注册 MyBatis-Plus 的插件（拦截器）：
 * <ul>
 *   <li><b>分页插件</b>：自动处理 Page 对象的 SQL 分页，无需手写 LIMIT</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // Service层中使用分页查询：
 * Page<UserInfo> page = new Page<>(1, 10);
 * IPage<UserInfo> result = userMapper.selectPage(page, null);
 * }</pre>
 *
 * @author 连梓祺 & 团队
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 注册 MyBatis-Plus 拦截器
     *
     * @return 配置好插件的拦截器链
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // ① 分页插件 - 支持多种数据库的分页
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(500L);  // 单页最大记录数限制（防止恶意请求拖垮数据库）
        interceptor.addInnerInterceptor(paginationInterceptor);

        return interceptor;
    }

}
