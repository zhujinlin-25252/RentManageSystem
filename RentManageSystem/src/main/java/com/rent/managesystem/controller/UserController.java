package com.rent.managesystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rent.managesystem.common.Result;
import com.rent.managesystem.common.ErrorCode;
import com.rent.managesystem.dto.PageResult;
import com.rent.managesystem.dto.UserVO;
import com.rent.managesystem.entity.UserInfo;
import com.rent.managesystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理控制器
 *
 * <p>处理用户信息查看/修改等接口。
 * 除获取当前用户信息外，其余接口需要管理员权限。
 *
 * @author 连梓祺 & 团队
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 获取当前登录用户的个人信息
     *
     * <p>从 SecurityContext 中获取当前登录的用户ID。
     *
     * <pre>{@code GET /api/user/info}</pre>
     */
    @GetMapping("/info")
    public Result<UserVO> getCurrentUser() {
        String userId = getCurrentUserId();
        UserInfo user = userService.getById(userId);
        if (user == null) {
            return Result.error(com.rent.managesystem.common.ErrorCode.USER_NOT_FOUND);
        }
        return Result.success(UserVO.fromEntity(user));
    }

    /**
     * 更新当前用户信息（昵称、头像等）
     *
     * <pre>{@code PUT /api/user/info
     * Request: { "nickname": "新昵称", "gender": 1, "avatarUrl": "..." }}</pre>
     */
    @PutMapping("/info")
    public Result<UserVO> updateInfo(@RequestBody UserInfo userInfo) {
        String userId = getCurrentUserId();
        UserInfo existing = userService.getById(userId);
        if (existing == null) {
            return Result.error(com.rent.managesystem.common.ErrorCode.USER_NOT_FOUND);
        }

        // 只允许更新非敏感字段
        if (userInfo.getNickname() != null) existing.setNickname(userInfo.getNickname());
        if (userInfo.getGender() != null) existing.setGender(userInfo.getGender());
        if (userInfo.getAvatarUrl() != null) existing.setAvatarUrl(userInfo.getAvatarUrl());

        userService.updateById(existing);
        return Result.success(UserVO.fromEntity(existing));
    }

    /**
     * 查询当前用户是否已设置支付密码
     *
     * <pre>{@code GET /api/user/has-payment-password}</pre>
     */
    @GetMapping("/has-payment-password")
    public Result<Boolean> hasPaymentPassword() {
        String userId = getCurrentUserId();
        UserInfo user = userService.getById(userId);
        if (user == null) {
            return Result.error(ErrorCode.USER_NOT_FOUND);
        }
        boolean has = user.getPaymentPassword() != null && !user.getPaymentPassword().isEmpty();
        return Result.success(has);
    }

    /**
     * 设置/修改支付密码
     *
     * <p>首次设置时 password 为空，仅需 newPassword。
     * 修改时需验证旧密码。
     *
     * <pre>{@code PUT /api/user/payment-password
     * Request: { "password": "123456", "newPassword": "654321" }
     * }</pre>
     */
    @PutMapping("/payment-password")
    public Result<Void> setPaymentPassword(@RequestBody Map<String, String> body) {
        String userId = getCurrentUserId();
        UserInfo user = userService.getById(userId);
        if (user == null) {
            return Result.error(ErrorCode.USER_NOT_FOUND);
        }

        String oldPassword = body.get("password");
        String newPassword = body.get("newPassword");

        if (newPassword == null || !newPassword.matches("\\d{6}")) {
            return Result.error(400, "支付密码必须为6位数字");
        }

        // 修改密码时验证旧密码
        if (oldPassword != null && !oldPassword.isEmpty()) {
            if (user.getPaymentPassword() == null
                    || !passwordEncoder.matches(oldPassword, user.getPaymentPassword())) {
                return Result.error(400, "原支付密码错误");
            }
        }

        user.setPaymentPassword(passwordEncoder.encode(newPassword));
        userService.updateById(user);
        return Result.success();
    }

    /**
     * 分页查询用户列表（管理员专用）
     *
     * <pre>{@code GET /api/user/list?page=1&size=10&role=0}</pre>
     */
    @GetMapping("/list")
    public Result<PageResult<UserVO>> listUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer role) {

        Page<UserInfo> mpPage = new Page<>(page, size);

        // 使用条件构造器按角色筛选
        if (role != null) {
            userService.lambdaQuery()
                    .eq(UserInfo::getRole, role)
                    .orderByDesc(UserInfo::getCreateTime)
                    .page(mpPage);
        } else {
            userService.lambdaQuery()
                    .orderByDesc(UserInfo::getCreateTime)
                    .page(mpPage);
        }

        // 转换为 VO 列表
        PageResult<UserVO> result = new PageResult<>();
        result.setTotal(mpPage.getTotal());
        result.setPage(mpPage.getCurrent());
        result.setSize(mpPage.getSize());
        result.setPages(mpPage.getPages());
        result.setRecords(
                mpPage.getRecords().stream().map(UserVO::fromEntity).toList()
        );

        return Result.success(result);
    }

    /**
     * 从 Spring Security 上下文中获取当前用户ID
     */
    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (String) auth.getPrincipal();  // 在JwtAuthFilter中设置的Principal就是userId
    }
}
