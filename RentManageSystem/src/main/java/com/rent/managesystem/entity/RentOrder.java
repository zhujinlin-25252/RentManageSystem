package com.rent.managesystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 租房订单实体类
 *
 * <p>对应数据库表：{@code rent_order}
 * <p>记录租客发起的租房申请，从"我想租房"到"签约完成"的全流程。
 *
 * @author 连梓祺 & 团队
 */
@Data
@TableName("rent_order")
public class RentOrder {

    /** 订单UUID（雪花算法自动生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private String orderId;

    /** 订单编号（业务唯一，如 RO202605190001） */
    @TableField("order_no")
    private String orderNo;

    /** 关联支付单ID → payment_order.order_id */
    @TableField("payment_order_id")
    private String paymentOrderId;

    /** 房源ID → house_info.house_id */
    @TableField("house_id")
    private String houseId;

    /** 租客用户ID → user_info.user_id */
    @TableField("tenant_id")
    private String tenantId;

    /** 房东用户ID → user_info.user_id */
    @TableField("landlord_id")
    private String landlordId;

    /** 订单标题（房源标题） */
    private String title;

    /** 月租金（元） */
    @TableField("rent_monthly")
    private BigDecimal rentMonthly;

    /** 押金（元） */
    private BigDecimal deposit;

    /** 租期开始日期 */
    @TableField("start_date")
    private LocalDate startDate;

    /** 租期结束日期 */
    @TableField("end_date")
    private LocalDate endDate;

    /** 订单总金额（元） */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /** 联系人姓名 */
    @TableField("contact_name")
    private String contactName;

    /** 联系电话 */
    @TableField("contact_phone")
    private String contactPhone;

    /** 备注/留言 */
    private String remark;

    /**
     * 状态：0=待确认，1=已确认/租住中，2=已完成，3=已取消(租客)，4=已拒绝(房东)
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
