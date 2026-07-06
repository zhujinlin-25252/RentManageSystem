package com.rent.managesystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户收藏关系实体类
 *
 * <p>对应数据库表：{@code user_favorite}
 * <p>多对多中间表，记录用户收藏了哪些房源。
 *
 * @author 连梓祺 & 团队
 */
@Data
@TableName("user_favorite")
public class UserFavorite {

    /** 自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 收藏用户ID → user_info.user_id */
    @TableField("user_id")
    private String userId;

    /** 被收藏的房源ID → house_info.house_id */
    @TableField("house_id")
    private String houseId;

    /** 收藏时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
