package com.rent.managesystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 支付订单实体类
 *
 * <p>对应数据库表：{@code payment_order}
 * <p>记录所有费用支付行为：押金、租金、物业费、水电费等。
 *
 * @author 连梓祺 & 团队
 */
@Data
@TableName("payment_order")
public class PaymentOrder {

    /** 订单UUID */
    @TableId(type = IdType.ASSIGN_ID)
    private String orderId;

    /** 订单编号（唯一） */
    @TableField("order_no")
    private String orderNo;

    /** 关联合同ID（押金和租金必有关联） */
    @TableField("contract_id")
    private String contractId;

    /** 付款人ID → user_info.user_id */
    @TableField("payer_id")
    private String payerId;

    /** 收款人ID → user_info.user_id */
    @TableField("payee_id")
    private String payeeId;

    /**
     * 订单类型：1=押金，2=租金，3=物业费，4=水电费，5=其他
     */
    @TableField("order_type")
    private Integer orderType;

    /** 订单金额（元） */
    private BigDecimal amount;

    /** 费用周期起始日 */
    @TableField("period_start")
    private LocalDate periodStart;

    /** 费用周期终止日 */
    @TableField("period_end")
    private LocalDate periodEnd;

    /**
     * 支付渠道：1=支付宝，2=微信支付，3=银行卡转账
     */
    @TableField("pay_channel")
    private Integer payChannel;

    /** 第三方交易流水号 */
    private String tradeNo;

    /**
     * 状态：0=待支付，1=支付中，2=已支付，3=已退款，4=已取消
     */
    private Integer status;

    /** 实际支付成功时间 */
    @TableField("paid_at")
    private LocalDateTime paidAt;

    /** 订单超时时间（30分钟未支付自动取消） */
    @TableField("expire_at")
    private LocalDateTime expireAt;

    /** 已退款金额 */
    @TableField("refund_amount")
    private BigDecimal refundAmount;

    /** 退款原因 */
    @TableField("refund_reason")
    private String refundReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
