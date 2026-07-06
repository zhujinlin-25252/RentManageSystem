package com.rent.managesystem.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收藏列表响应DTO
 *
 * <p>包含收藏记录基本信息和关联的房源展示字段，
 * 用于"我的收藏"页面的卡片列表渲染。
 *
 * @author 连梓祺 & 团队
 */
@Data
public class FavoriteVO {

    /** 收藏记录ID */
    private Long id;

    /** 房源ID */
    private String houseId;

    /** 收藏时间 */
    private LocalDateTime createTime;

    // ==================== 房源展示字段 ====================

    /** 房源标题 */
    private String title;

    /** 封面图片URL */
    private String coverImage;

    /** 详细地址 */
    private String address;

    /** 城市 */
    private String city;

    /** 区域 */
    private String district;

    /** 月租金 */
    private BigDecimal rentMonthly;

    /** 面积（平方米） */
    private BigDecimal area;

    /** 室数 */
    private Integer rooms;

    /** 厅数 */
    private Integer halls;

    /** 朝向 */
    private String orientation;

    /** 房源状态（0=待审核 1=已发布 2=已出租 3=下架 4=审核拒绝） */
    private Integer status;
}
