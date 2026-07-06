package com.rent.managesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rent.managesystem.entity.HouseInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 房源信息 Mapper
 *
 * @author 连梓祺 & 团队
 */
@Mapper
public interface HouseMapper extends BaseMapper<HouseInfo> {
}
