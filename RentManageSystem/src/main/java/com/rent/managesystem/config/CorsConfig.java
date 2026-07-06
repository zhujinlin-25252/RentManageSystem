package com.rent.managesystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域资源共享(CORS)配置
 *
 * <p>前后端分离架构中，前端(Vue运行在 localhost:5173) 调用后端API(localhost:8080) 时，
 * 浏览器会因为"同源策略"拦截请求。此配置允许前端跨域访问后端接口。
 *
 * <p><b>生产环境建议：</b>将 allowedOrigins 改为具体的前端域名，
 * 不要使用 "*" （会携带Cookie时浏览器不允许通配符）
 *
 * @author 连梓祺 & 团队
 */
@Configuration
public class CorsConfig {

    /**
     * 配置CORS过滤器
     *
     * @return CorsFilter 实例
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许的源地址（开发环境）
        config.addAllowedOrigin("http://localhost:5173");   // Vite 默认端口
        config.addAllowedOrigin("http://localhost:5174");   // 备用端口（5173被占时）
        config.addAllowedOrigin("http://localhost:3000");   // 备用前端端口
        config.addAllowedOrigin("http://127.0.0.1:5173");
        config.addAllowedOrigin("http://127.0.0.1:5174");

        // 允许携带 Cookie/Authorization 头
        config.setAllowCredentials(true);

        // 允许所有HTTP方法（GET/POST/PUT/DELETE等）
        config.addAllowedMethod("*");

        // 允许所有请求头（包括自定义Header如 Token、X-Requested-With 等）
        config.addAllowedHeader("*");

        // 预检请求(OPTIONS)缓存时间(秒)，减少OPTIONS请求数量
        config.setMaxAge(3600L);

        // 应用到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        return new CorsFilter(source);
    }

}
