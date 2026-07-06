package com.rent.managesystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 操作审计日志实体类
 *
 * <p>对应数据库表：{@code operation_log}
 * <p>记录系统所有关键操作，用于合规审计和安全追溯。
 * 不设外键关联，避免影响业务表性能。
 *
 * @author 连梓祺 & 团队
 */
@Data
@TableName("operation_log")
public class OperationLog {

    /** 日志自增ID */
    @TableId(type = IdType.AUTO)
    private Long logId;

    /** 操作用户ID（匿名接口可为空） */
    @TableField("user_id")
    private String userId;

    /** 操作用户名（冗余存一份，防止用户删除后无法追溯） */
    private String username;

    /** 操作模块：auth/user/house/contract/payment/repair/expense/notice/admin */
    private String module;

    /** 操作动作：login/logout/register/create/update/delete/audit/sign/pay... */
    private String action;

    /** 请求方法全限定名（如 com.xxx.controller.UserController.login） */
    private String method;

    /** 请求URL路径 */
    private String url;

    /** HTTP动词：GET/POST/PUT/DELETE */
    @TableField("http_method")
    private String httpMethod;

    /** 客户端IP地址 */
    private String ip;

    /** 请求参数（敏感字段已脱敏后的JSON） */
    @TableField("request_params")
    private String requestParams;

    /** 响应结果摘要（调试用） */
    @TableField("response_result")
    private String responseResult;

    /** 接口执行耗时（毫秒） */
    @TableField("execution_time")
    private Long executionTime;

    /** 是否成功：0=失败，1=成功 */
    private Integer success;

    /** 错误信息（仅失败时记录） */
    @TableField("error_msg")
    private String errorMsg;

    /** 客户端User-Agent标识 */
    @TableField("user_agent")
    private String userAgent;

    /** 日志产生时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
