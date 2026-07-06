package com.rent.managesystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rent.managesystem.entity.PaymentOrder;
import com.rent.managesystem.mapper.PaymentOrderMapper;
import com.rent.managesystem.service.PaymentOrderService;
import org.springframework.stereotype.Service;

/**
 * 支付订单 Service 实现类
 *
 * @author 连梓祺 & 团队
 */
@Service
public class PaymentOrderServiceImpl extends ServiceImpl<PaymentOrderMapper, PaymentOrder> implements PaymentOrderService {
}
