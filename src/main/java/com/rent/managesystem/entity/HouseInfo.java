package com.rent.managesystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 房源信息实体类
 *
 * <p>对应数据库表：{@code house_info}
 * <p>房东发布的房源，包含位置、户型、价格、状态等核心信息。
 *
 * @author 连梓祺 & 团队
 */
@Data
@TableName("house_info")
public class HouseInfo {

    /** 房源UUID（雪花算法自动生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private String houseId;

    /** 房东用户ID → user_info.user_id */
    @TableField("landlord_id")
    private String landlordId;

    private String province;
    private String city;
    private String district;
    private String address;

    /** 纬度（用于地图展示和距离计算） */
    private BigDecimal latitude;

    /** 经度（用于地图展示和距离计算） */
    private BigDecimal longitude;

    /**
     * 房源类型：1=整租，2=合租
     */
    @TableField("house_type")
    private Integer houseType;

    /** 建筑面积（㎡） */
    private BigDecimal area;

    /** 房间数（室） */
    private Integer rooms;

    /** 厅数 */
    private Integer halls;

    /** 卫生间数 */
    private Integer bathrooms;

    /** 楼层描述（如"6层/18层"） */
    private String floor;

    /** 朝向（东/南/西/北/南北/东南等） */
    private String orientation;

    /** 月租金（元） */
    @TableField("rent_monthly")
    private BigDecimal rentMonthly;

    /** 押金（元） */
    private BigDecimal deposit;

    /**
     * 推荐支付方式：0=月付，1=季付，2=半年付，3=年付
     */
    @TableField("payment_type")
    private Integer paymentType;

    /** 配套设施JSON数组（如 ["空调","洗衣机","WiFi"]） */
    private String facilities;

    /** 房源标题 */
    private String title;

    /** 详细描述（Markdown格式） */
    private String description;

    /** 封面图片URL */
    @TableField("cover_image")
    private String coverImage;

    /**
     * 状态：0=待审核，1=已发布，2=已出租，3=下架，4=审核拒绝
     */
    private Integer status;

    /** 审核人ID */
    @TableField("audit_user_id")
    private String auditUserId;

    /** 审核时间 */
    @TableField("audit_time")
    private LocalDateTime auditTime;

    /** 审核备注/拒绝原因 */
    @TableField("audit_remark")
    private String auditRemark;

    /** 浏览次数 */
    @TableField("view_count")
    private Integer viewCount;

    /** 收藏人数 */
    @TableField("favorite_count")
    private Integer favoriteCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    /**
     * 前端传入的标志：编辑后内容是否有变更
     * 非数据库字段，仅用于 updateHouse 接口接收参数
     */
    @TableField(exist = false)
    private Boolean needReaudit;
}
