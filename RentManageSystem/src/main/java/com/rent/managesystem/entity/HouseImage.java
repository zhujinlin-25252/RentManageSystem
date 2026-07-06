package com.rent.managesystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 房源图片实体类
 *
 * <p>对应数据库表：{@code house_image}
 * <p>一个房源可有多张图片（最多9张），支持设置封面图。
 *
 * @author 连梓祺 & 团队
 */
@Data
@TableName("house_image")
public class HouseImage {

    /** 图片记录ID（自增主键） */
    @TableId(type = IdType.AUTO)
    private Long imageId;

    /** 所属房源ID → house_info.house_id */
    @TableField("house_id")
    private String houseId;

    /** 图片URL（本地路径或OSS地址） */
    @TableField("image_url")
    private String imageUrl;

    /** 排序序号（越小越靠前） */
    @TableField("sort_order")
    private Integer sortOrder;

    /** 是否为封面图：0=否，1=是 */
    private Integer isCover;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
