package com.rent.managesystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security 安全配置
 *
 * <p>当前版本功能：
 * <ul>
 *   <li>CORS 跨域支持（Vue前端 localhost:5173）</li>
 *   <li>JWT Token 认证（通过 JwtAuthFilter 过滤器）</li>
 *   <li>公开接口白名单（登录/注册/房源浏览/API文档等无需Token）</li>
 *   <li>BCrypt 密码编码器</li>
 * </ul>
 *
 * @author 连梓祺 & 团队
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 安全过滤链配置（核心！决定哪些接口需要认证）
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           CorsConfigurationSource corsConfigurationSource,
                                           JwtAuthFilter jwtAuthFilter) throws Exception {
        http
            // 启用CORS（使用我们自己的CorsConfig配置）
            .cors(cors -> cors.configurationSource(corsConfigurationSource))

            // 禁用 CSRF（前后端分离+JWT方案不需要CSRF保护）
            .csrf(csrf -> csrf.disable())

            // ========== 添加JWT过滤器（在用户名密码过滤器之前执行） ==========
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

            // ========== 接口权限配置 ==========
            .authorizeHttpRequests(auth -> auth
                // 🔓 公开接口（无需Token即可访问）
                // 注意：这里同时列出带/不带 /api 前缀的路径，确保兼容性
                .requestMatchers(
                    "/auth/login",
                    "/auth/register",
                    "/auth/check-phone",
                    "/auth/send-code",
                    "/house/public/**",
                    "/notice/public/**",
                    "/upload/images/**",     // 房源图片查询（游客浏览也需要）
                    "/uploads/**",          // 静态资源：上传的图片（无需登录即可查看）
                    "/doc.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/webjars/**",
                    "/favicon.ico"
                ).permitAll()
                // 兼容：也允许带 /api 前缀的路径（某些 Spring 版本需要）
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/auth/check-phone",
                    "/api/auth/send-code",
                    "/api/house/public/**",
                    "/api/notice/public/**",
                    "/api/upload/images/**",   // 房源图片查询（游客浏览也需要）
                    "/api/uploads/**",       // 静态资源：上传的图片
                    "/api/doc.html",
                    "/api/swagger-ui/**",
                    "/api/v3/api-docs/**"
                ).permitAll()

                // 🔐 其他所有接口都需要JWT认证
                .anyRequest().authenticated()
            )

            // 禁用默认的登录页面和基础认证（使用JWT方式）
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }

    /**
     * 密码编码器（BCrypt算法）
     *
     * <p>BCrypt 是目前业界推荐的密码哈希算法：
     * <ul>
     *   <li>内置随机盐值 → 同一密码每次加密结果都不同</li>
     *   <li>可调计算强度(strength) → 即使算力提升也难以暴力破解</li>
     * </ul>
     *
     * @return BCryptPasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
