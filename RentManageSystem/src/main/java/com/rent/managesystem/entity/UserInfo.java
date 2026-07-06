package com.rent.managesystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户信息实体类
 *
 * <p>对应数据库表：{@code user_info}
 * <p>存储系统所有注册用户的基础信息、认证状态和账户安全属性。
 *
 * <p><b>字段说明：</b>
 * <ul>
 *   <li>主键使用雪花算法自动生成（由 MyBatis-Plus 的 ASSIGN_ID 策略管理）</li>
 *   <li>密码存储 BCrypt 哈希值（永远不存明文）</li>
 *   <li>支持逻辑删除（deleted 字段）</li>
 * </ul>
 *
 * @author 连梓祺 & 团队
 * @see com.rent.managesystem.mapper.UserMapper
 */
@Data
@TableName("user_info")
public class UserInfo {

    /** 用户UUID（雪花算法自动生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private String userId;

    /** 手机号（中国大陆11位，唯一） */
    private String phone;

    /** 邮箱地址（唯一） */
    private String email;

    /** BCrypt加密后的密码哈希值（长度255以容纳BCrypt最长输出） */
    @TableField("password_hash")
    private String passwordHash;

    /** 支付密码（BCrypt加密，6位数字） */
    @TableField("payment_password")
    private String paymentPassword;

    /** 用户昵称（对外展示名称） */
    private String nickname;

    /** 真实姓名（AES加密存储，脱敏展示） */
    @TableField("real_name")
    private String realName;

    /** 身份证号（AES加密存储） */
    private String idCard;

    /** 头像图片URL（本地/OSS路径） */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 角色：0=租客，1=房东，2=管理员，3=客服
     */
    private Integer role;

    /**
     * 账户状态：0=未激活，1=正常，2=禁用
     */
    private Integer status;

    /** 实名认证状态：0=未认证，1=已认证 */
    @TableField("is_verified")
    private Integer isVerified;

    /** 认证通过时间 */
    @TableField("verify_time")
    private LocalDateTime verifyTime;

    /** 最后登录时间 */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /** 最后登录IP（兼容IPv6） */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /** 连续登录失败次数（用于防暴力破解） */
    @TableField("login_fail_count")
    private Integer loginFailCount;

    /** 账户锁定截止时间（null表示未锁定） */
    @TableField("login_lock_until")
    private LocalDateTime loginLockUntil;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间（自动填充+自动更新） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标志：0=正常，1=已删除 */
    @TableLogic
    private Integer deleted;
}
