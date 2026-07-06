package com.rent.managesystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合同信息实体类
 *
 * <p>对应数据库表：{@code contract_info}
 * <p>租客与房东之间的租赁合同记录。
 *
 * @author 连梓祺 & 团队
 */
@Data
@TableName("contract_info")
public class ContractInfo {

    /** 合同UUID */
    @TableId(type = IdType.ASSIGN_ID)
    private String contractId;

    /** 合同编号（人类可读唯一编号，如 CT20260510001） */
    @TableField("contract_no")
    private String contractNo;

    /** 房源ID → house_info.house_id */
    @TableField("house_id")
    private String houseId;

    /** 租客ID → user_info.user_id */
    @TableField("tenant_id")
    private String tenantId;

    /** 房东ID → user_info.user_id */
    @TableField("landlord_id")
    private String landlordId;

    /** 租赁起始日期 */
    @TableField("start_date")
    private LocalDate startDate;

    /** 租赁终止日期 */
    @TableField("end_date")
    private LocalDate endDate;

    /** 租赁总月数 */
    @TableField("duration_months")
    private Integer durationMonths;

    /** 月租金快照（签约时锁定当时的金额） */
    @TableField("monthly_rent")
    private BigDecimal monthlyRent;

    /** 押金总额 */
    @TableField("deposit_amount")
    private BigDecimal depositAmount;

    /** 合同总金额 */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 支付周期：0=月付，1=季付，2=半年付，3=年付
     */
    @TableField("payment_method")
    private Integer paymentMethod;

    /** 合同正文内容（Markdown格式） */
    @TableField("content_text")
    private String contentText;

    /** 租客电子签名URL */
    @TableField("tenant_signature")
    private String tenantSignature;

    /** 房东电子签名URL */
    @TableField("landlord_signature")
    private String landlordSignature;

    /**
     * 状态：0=草稿，1=待租客签，2=待房东签，3=生效中，4=已到期，5=已解除
     */
    private Integer status;

    /** PDF版合同URL */
    private String pdfUrl;

    /** 签署完成时间 */
    @TableField("signed_at")
    private LocalDateTime signedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
