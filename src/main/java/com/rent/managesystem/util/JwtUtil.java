package com.rent.managesystem.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT（JSON Web Token）工具类
 *
 * <p>负责 Token 的生成、解析和校验。JWT 用于前后端分离架构中的无状态认证。
 *
 * <p><b>Token 结构：</b>
 * <pre>
 *   Header.Payload.Signature
 *   ├── Header:  {"alg":"HS256","typ":"JWT"}
 *   ├── Payload: {"userId":"xxx","role":1,"exp":170xxxx}
 *   └── Signature: HMAC-SHA256(Header + Payload, 密钥)
 * </pre>
 *
 * <p><b>使用流程：</b>
 * <ol>
 *   <li>用户登录成功 → 生成JWT返回给前端</li>
 *   <li>前端后续请求在 Header 中携带：{@code Authorization: Bearer xxx}</li>
 *   <li>后端过滤器解析Token → 获取用户身份 → 放行请求</li>
 * </ol>
 *
 * @author 连梓祺 & 团队
 */
@Component
public class JwtUtil {

    /** JWT签名密钥（从配置文件读取，生产环境必须用强随机字符串！） */
    @Value("${jwt.secret:rent-manage-system-secret-key-2026-must-be-changed-in-production}")
    private String secret;

    /** Token有效期（毫秒）：默认7天 */
    @Value("${jwt.expiration:604800000}")
    private long expiration;

    /**
     * 生成 HMAC-SHA256 签名密钥对象
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     *
     * @param userId 用户ID
     * @param phone  手机号（用于日志追溯）
     * @param role   用户角色编码
     * @return JWT令牌字符串
     */
    public String generateToken(String userId, String phone, Integer role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userId)                    // subject = 用户ID
                .claim("phone", phone)              // 自定义载荷：手机号
                .claim("role", role)                // 自定义载荷：角色
                .issuedAt(now)                      // 签发时间
                .expiration(expiryDate)             // 过期时间
                .signWith(getSigningKey())          // 签名算法+密钥
                .compact();
    }

    /**
     * 解析 JWT Token，获取 Claims（所有载荷信息）
     *
     * @param token JWT字符串
     * @return Claims 对象（包含userId/phone/role/exp等）
     * @throws io.jsonwebtoken.JwtException Token无效或已过期时抛出异常
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token JWT字符串
     * @return 用户ID，解析失败返回null
     */
    public String getUserId(String token) {
        try {
            return parseToken(token).getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从Token中获取手机号
     */
    public String getPhone(String token) {
        try {
            return parseToken(token).get("phone", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从Token中获取用户角色
     */
    public Integer getRole(String token) {
        try {
            return parseToken(token).get("role", Integer.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 校验 Token 是否有效（未过期且格式正确）
     *
     * @param token JWT字符串
     * @return true=有效，false=无效或已过期
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取Token过期时间戳
     */
    public long getExpirationTime() {
        return System.currentTimeMillis() + expiration;
    }
}
