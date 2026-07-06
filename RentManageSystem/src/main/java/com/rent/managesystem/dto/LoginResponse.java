package com.rent.managesystem.dto;

import lombok.Data;

/**
 * 登录成功响应DTO（包含JWT Token）
 *
 * @author 连梓祺 & 团队
 */
@Data
public class LoginResponse {

    /** JWT访问令牌 */
    private String token;

    /** Token过期时间戳（毫秒） */
    private Long expireTime;

    /** 用户基本信息 */
    private UserVO userInfo;

    /**
     * 快速创建登录响应
     *
     * @param token     JWT令牌
     * @param expireTime 过期时间
     * @param user      用户信息
     * @return 登录响应对象
     */
    public static LoginResponse ok(String token, Long expireTime, UserVO user) {
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setExpireTime(expireTime);
        response.setUserInfo(user);
        return response;
    }
}
