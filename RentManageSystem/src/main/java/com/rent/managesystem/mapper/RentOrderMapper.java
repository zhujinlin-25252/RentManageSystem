package com.rent.managesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rent.managesystem.entity.RentOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租房订单 Mapper
 *
 * @author 连梓祺 & 团队
 */
@Mapper
public interface RentOrderMapper extends BaseMapper<RentOrder> {
}
