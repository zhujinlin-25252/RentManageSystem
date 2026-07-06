package com.rent.managesystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 费用记录实体类
 *
 * <p>对应数据库表：{@code expense_record}
 * <p>记录水电气、物业、网费等日常费用的产生与缴纳情况。
 *
 * @author 连梓祺 & 团队
 */
@Data
@TableName("expense_record")
public class ExpenseRecord {

    /** 费用记录UUID */
    @TableId(type = IdType.ASSIGN_ID)
    private String expenseId;

    /** 费用编号（唯一） */
    @TableField("expense_no")
    private String expenseNo;

    /** 房屋ID → house_info.house_id */
    @TableField("house_id")
    private String houseId;

    /** 租客ID（缴费责任人）→ user_info.user_id */
    @TableField("tenant_id")
    private String tenantId;

    /**
     * 费用类型：1=水费，2=电费，3=燃气费，4=物业费，5=网费，6=其他
     */
    @TableField("expense_type")
    private Integer expenseType;

    /** 金额（元） */
    private BigDecimal amount;

    /** 计费周期起始日 */
    @TableField("period_start")
    private LocalDate periodStart;

    /** 计费周期终止日 */
    @TableField("period_end")
    private LocalDate periodEnd;

    /** 上期读数（用于水电气等按量计费） */
    @TableField("reading_previous")
    private BigDecimal readingPrevious;

    /** 本期读数 */
    @TableField("reading_current")
    private BigDecimal readingCurrent;

    /** 单价 */
    @TableField("unit_price")
    private BigDecimal unitPrice;

    /**
     * 状态：0=待缴，1=已缴，2=作废
     */
    private Integer status;

    /** 关联的支付订单ID → payment_order.order_id */
    @TableField("payment_order_id")
    private String paymentOrderId;

    /** 实际缴纳时间 */
    @TableField("paid_at")
    private LocalDateTime paidAt;

    /** 录入人ID（通常是房东）→ user_info.user_id */
    @TableField("creator_id")
    private String creatorId;

    /** 备注说明 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
