package com.rent.managesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rent.managesystem.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户收藏 Mapper
 *
 * @author 连梓祺 & 团队
 */
@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {
}
