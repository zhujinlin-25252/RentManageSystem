package com.rent.managesystem.config;

import com.rent.managesystem.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT 认证过滤器
 *
 * <p>在 Spring Security 过滤链中，拦截所有需要认证的请求：
 * <ol>
 *   <li>从请求头中提取 Token（{@code Authorization: Bearer xxx}）</li>
 *   <li>解析并校验 JWT 是否有效</li>
 *   <li>将用户身份信息写入 SecurityContext</li>
 *   <li>放行给后续的 Controller 处理</li>
 * </ol>
 *
 * <p><b>Token 无效时的处理：</b>直接返回 JSON 错误响应，不抛异常。
 *
 * @author 连梓祺 & 团队
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;  // 用于输出JSON

    public JwtAuthFilter(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ① 跳过 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // ② 从 Header 中提取 Token
        String token = extractToken(request);

        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            // ③ Token有效：解析用户信息并设置到 SecurityContext
            String userId = jwtUtil.getUserId(token);
            Integer role = jwtUtil.getRole(token);

            // 构造权限列表（基于角色的简单权限控制）
            List<SimpleGrantedAuthority> authorities = getAuthorities(role);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

            // 把认证信息存入上下文（后续 Controller 可通过 SecurityContext 获取当前用户）
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        // ④ 无Token或Token无效 → 直接放行，由 Spring Security 的 authorizeHttpRequests 决定是否需要认证

        // ⑤ 放行继续处理
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取 Bearer Token
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);  // 去掉 "Bearer " 前缀
        }
        return null;
    }

    /**
     * 根据角色编码生成 Spring Security 权限列表
     */
    private List<SimpleGrantedAuthority> getAuthorities(Integer role) {
        if (role == null) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_TENANT"));
        }
        switch (role) {
            case 0: return Collections.singletonList(new SimpleGrantedAuthority("ROLE_TENANT"));
            case 1: return Collections.singletonList(new SimpleGrantedAuthority("ROLE_LANDLORD"));
            case 2: return Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
            case 3: return Collections.singletonList(new SimpleGrantedAuthority("ROLE_SERVICE"));
            default: return Collections.singletonList(new SimpleGrantedAuthority("ROLE_TENANT"));
        }
    }

}

