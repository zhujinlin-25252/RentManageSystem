package com.rent.managesystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rent.managesystem.common.exception.BizException;
import com.rent.managesystem.common.ErrorCode;
import com.rent.managesystem.entity.UserInfo;
import com.rent.managesystem.mapper.UserMapper;
import com.rent.managesystem.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户 Service 实现类
 *
 * <p>实现用户相关的核心业务逻辑：
 * <ul>
 *   <li>注册、登录、信息修改</li>
 *   <li>密码校验（BCrypt比对）</li>
 *   <li>账户安全（登录失败计数、锁定判断）</li>
 * </ul>
 *
 * @author 连梓祺 & 团队
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserInfo> implements UserService {

    /** Spring Security 的 BCrypt 密码编码器（在 SecurityConfig 中已配置 Bean） */
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 根据手机号查询用户
     */
    @Override
    public UserInfo getByPhone(String phone) {
        return lambdaQuery()
                .eq(UserInfo::getPhone, phone)
                .last("LIMIT 1")
                .one();
    }

    /**
     * 用户登录验证
     *
     * <p>流程：
     * <ol>
     *   <li>根据手机号查找用户 → 不存在则提示</li>
     *   <li>检查账户是否被禁用或锁定</li>
     *   <li>比对密码（BCrypt）</li>
     *   <li>更新最后登录时间和IP</li>
     * </ol>
     */
    @Override
    public UserInfo login(String phone, String rawPassword) {
        // ① 查找用户
        UserInfo user = getByPhone(phone);
        if (user == null) {
            throw new BizException(ErrorCode.LOGIN_FAILED);
        }

        // ② 检查账户状态
        if (user.getStatus() == 2) {
            throw new BizException(ErrorCode.USER_DISABLED);
        }
        if (user.getStatus() == 0) {
            throw new BizException(ErrorCode.USER_NOT_FOUND); // 未激活也视为找不到
        }
        // ③ 检查是否被锁定
        if (user.getLoginLockUntil() != null && user.getLoginLockUntil().isAfter(java.time.LocalDateTime.now())) {
            throw new BizException(ErrorCode.USER_LOCKED);
        }

        // ④ 校验密码
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            // 密码错误：增加失败次数，达到5次锁定30分钟
            int failCount = user.getLoginFailCount() + 1;
            user.setLoginFailCount(failCount);
            if (failCount >= 5) {
                user.setLoginLockUntil(java.time.LocalDateTime.now().plusMinutes(30));
                updateById(user);
                throw new BizException(ErrorCode.USER_LOCKED);
            }
            updateById(user);
            throw new BizException(ErrorCode.LOGIN_FAILED);
        }

        // ⑤ 登录成功：重置失败次数 + 更新登录时间
        user.setLoginFailCount(0);
        user.setLoginLockUntil(null);
        user.setLastLoginTime(java.time.LocalDateTime.now());
        // IP 可以通过 AOP 或拦截器设置，这里暂时跳过
        updateById(user);

        return user;
    }

}
