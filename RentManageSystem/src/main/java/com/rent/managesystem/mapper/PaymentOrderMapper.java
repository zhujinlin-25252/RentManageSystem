package com.rent.managesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rent.managesystem.entity.PaymentOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付订单 Mapper
 *
 * @author 连梓祺 & 团队
 */
@Mapper
public interface PaymentOrderMapper extends BaseMapper<PaymentOrder> {
}
