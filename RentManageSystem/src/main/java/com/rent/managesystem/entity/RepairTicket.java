package com.rent.managesystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 报修工单实体类
 *
 * <p>对应数据库表：{@code repair_ticket}
 * <p>租客提交的房屋维修请求，由客服或维修人员处理。
 *
 * @author 连梓祺 & 团队
 */
@Data
@TableName("repair_ticket")
public class RepairTicket {

    /** 工单UUID */
    @TableId(type = IdType.ASSIGN_ID)
    private String ticketId;

    /** 工单编号（唯一） */
    @TableField("ticket_no")
    private String ticketNo;

    /** 房屋ID → house_info.house_id */
    @TableField("house_id")
    private String houseId;

    /** 报修人ID（通常是租客）→ user_info.user_id */
    @TableField("reporter_id")
    private String reporterId;

    /** 处理人ID（客服或维修人员）→ user_info.user_id */
    @TableField("handler_id")
    private String handlerId;

    /** 问题分类（如：水电、门窗、家电、管道、其他） */
    @TableField("problem_type")
    private String problemType;

    /** 报修标题 */
    private String title;

    /** 问题描述（支持图文混排Markdown） */
    private String description;

    /** 现场照片URL数组JSON */
    private String images;

    /**
     * 优先级：1=普通（48h内响应），2=紧急（24h），3=非常紧急（4h）
     */
    private Integer priority;

    /**
     * 状态：0=待处理，1=处理中，2=已完成，3=已关闭
     */
    private Integer status;

    /** 处理回复内容 */
    @TableField("handler_reply")
    private String handlerReply;

    /** 解决完成时间 */
    @TableField("resolved_at")
    private LocalDateTime resolvedAt;

    /** 关闭时间 */
    @TableField("closed_at")
    private LocalDateTime closedAt;

    /** 满意度评分（1-5分，NULL表示未评价） */
    private Integer satisfaction;

    /** 评价反馈文字 */
    private String feedback;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
