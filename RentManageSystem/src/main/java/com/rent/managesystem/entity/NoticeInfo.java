package com.rent.managesystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 公告通知实体类
 *
 * <p>对应数据库表：{@code notice_info}
 * <p>系统管理员发布的各类公告、活动通知、紧急通知等。
 *
 * @author 连梓祺 & 团队
 */
@Data
@TableName("notice_info")
public class NoticeInfo {

    /** 公告UUID */
    @TableId(type = IdType.ASSIGN_ID)
    private String noticeId;

    /** 公告标题 */
    private String title;

    /** 公告正文（支持Markdown富文本） */
    private String content;

    /**
     * 类型：0=系统公告，1=活动通知，2=紧急通知
     */
    @TableField("notice_type")
    private Integer noticeType;

    /**
     * 发布范围：0=全体用户，1=指定区域，2=指定房屋
     */
    private Integer scope;

    /** 范围具体值（区域名或房屋ID列表JSON） */
    @TableField("scope_value")
    private String scopeValue;

    /** 是否置顶：0=否，1=是 */
    private Integer isTop;

    /** 发布人ID → user_info.user_id */
    @TableField("publisher_id")
    private String publisherId;

    /** 阅读次数统计 */
    @TableField("view_count")
    private Integer viewCount;

    /** 发布时间（支持定时发布） */
    @TableField("publish_at")
    private LocalDateTime publishAt;

    /** 过期时间（NULL表示永不过期） */
    @TableField("expired_at")
    private LocalDateTime expiredAt;

    /**
     * 状态：0=草稿，1=已发布，2=已撤回
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
