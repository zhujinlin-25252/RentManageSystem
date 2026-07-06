package com.rent.managesystem.dto;

import com.rent.managesystem.entity.UserInfo;
import com.rent.managesystem.util.DesensitizeUtil;
import lombok.Data;

/**
 * 用户信息响应DTO（返回给前端）
 *
 * <p>从 Entity 转换而来，隐藏敏感字段如密码哈希。
 * 对真实姓名等敏感字段做脱敏处理。
 *
 * @author 连梓祺 & 团队
 */
@Data
public class UserVO {

    private String userId;
    private String phone;
    private String email;
    private String nickname;
    private String avatarUrl;
    private Integer gender;
    private Integer role;          // 角色编码
    private String roleName;      // 角色中文名（租客/房东/管理员/客服）
    private Integer status;        // 账户状态
    private Integer isVerified;   // 实名认证状态
    private String realName;      // 真实姓名（已脱敏，如 "张*"）

    /**
     * 从 Entity 转换为 VO
     *
     * @param user 用户实体
     * @return 响应VO（不含密码等敏感字段，真实姓名已脱敏）
     */
    public static UserVO fromEntity(UserInfo user) {
        UserVO vo = new UserVO();
        vo.setUserId(user.getUserId());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setGender(user.getGender());
        vo.setRole(user.getRole());
        vo.setRoleName(getRoleName(user.getRole()));
        vo.setStatus(user.getStatus());
        vo.setIsVerified(user.getIsVerified());
        // 真实姓名脱敏后输出
        if (user.getRealName() != null && !user.getRealName().isEmpty()) {
            vo.setRealName(DesensitizeUtil.name(user.getRealName()));
        }
        return vo;
    }

    /** 根据角色编码返回中文名称 */
    public static String getRoleName(Integer role) {
        if (role == null) return "未知";
        switch (role) {
            case 0: return "租客";
            case 1: return "房东";
            case 2: return "系统管理员";
            case 3: return "客服";
            default: return "未知";
        }
    }
}
