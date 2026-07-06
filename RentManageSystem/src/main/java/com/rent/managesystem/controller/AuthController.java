package com.rent.managesystem.controller;

import com.rent.managesystem.common.ErrorCode;
import com.rent.managesystem.common.Result;
import com.rent.managesystem.dto.LoginRequest;
import com.rent.managesystem.dto.LoginResponse;
import com.rent.managesystem.dto.RegisterRequest;
import com.rent.managesystem.dto.UserVO;
import com.rent.managesystem.entity.UserInfo;
import com.rent.managesystem.service.UserService;
import com.rent.managesystem.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * <p>处理用户注册、登录、登出等认证相关接口。
 * 所有接口都在 {@code SecurityConfig} 中配置为公开访问（无需Token）。
 *
 * @author 连梓祺 & 团队
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户注册接口
     *
     * <p>流程：校验参数 → 检查手机号是否已注册 → BCrypt加密密码 → 保存到数据库
     *
     * <pre>{@code POST /api/auth/register
     * Request: { "phone": "13800138000", "password": "Abc12345", "nickname": "新用户" }
     * Response: { "code": 200, "message": "操作成功", "data": null }
     * }</pre>
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        // ① 检查手机号是否已注册
        UserInfo existingUser = userService.getByPhone(request.getPhone());
        if (existingUser != null) {
            return Result.error(ErrorCode.PHONE_ALREADY_EXISTS);
        }

        // ② 构建用户对象并保存
        UserInfo user = new UserInfo();
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword())); // BCrypt加密
        user.setNickname(request.getNickname());
        if (request.getRealName() != null) user.setRealName(request.getRealName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        user.setGender(request.getGender() != null ? request.getGender() : 0);
        user.setRole(request.getRole() != null ? request.getRole() : 0);
        user.setStatus(1);         // 默认状态：正常（直接激活，无需额外验证）
        user.setIsVerified(0);     // 未实名

        boolean saved = userService.save(user);
        return saved ? Result.success() : Result.error(ErrorCode.SYSTEM_ERROR);
    }

    /**
     * 检查手机号是否已注册
     *
     * <p>供前端实时校验使用，手机号格式不合法时直接返回 false
     *
     * <pre>{@code GET /api/auth/check-phone?phone=13800138000
     * Response: { "code": 200, "data": true }
     * }</pre>
     */
    @GetMapping("/check-phone")
    public Result<Boolean> checkPhone(@RequestParam String phone) {
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            return Result.success(false);
        }
        UserInfo existingUser = userService.getByPhone(phone);
        return Result.success(existingUser != null);
    }

    /**
     * 用户登录接口
     *
     * <p>流程：校验参数 → 调用Service登录验证 → 生成JWT Token → 返回Token和用户信息
     *
     * <pre>{@code POST /api/auth/login
     * Request: { "phone": "13811111111", "password": "123456" }
     * Response: { "code": 200, "token": "eyJ...", "expireTime": ..., "userInfo": {...} }
     * }</pre>
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // 调用 Service 层的登录方法（包含密码校验+账户状态检查）
        UserInfo user = userService.login(request.getPhone(), request.getPassword());

        // 生成 JWT Token
        String token = jwtUtil.generateToken(user.getUserId(), user.getPhone(), user.getRole());

        // 组装响应
        UserVO userVO = UserVO.fromEntity(user);
        LoginResponse response = LoginResponse.ok(token, jwtUtil.getExpirationTime(), userVO);

        return Result.success(response);
    }
}
