package com.rent.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户登录请求DTO
 *
 * @author 连梓祺 & 团队
 */
@Data
public class LoginRequest {

    /** 手机号（必填，11位） */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 密码（必填） */
    @NotBlank(message = "密码不能为空")
    private String password;
}
